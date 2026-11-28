package dev.creeperkatze.exchange.dh;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jspecify.annotations.Nullable;
import org.sqlite.JDBC;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;

public final class DhDatabase implements AutoCloseable {
	public static final String FILE_NAME = "DistantHorizons.sqlite";
	private static final String SCRIPT_FOLDER = "sqlScripts/";
	private static final int WRITES_PER_COMMIT = 64;
	private static final String COLUMNS = "PosX, PosZ, DataFormatVersion, CompressionMode, Data, NorthAdjData, SouthAdjData, EastAdjData, WestAdjData, "
		+ "ColumnGenerationStep, ColumnWorldCompressionMode, Mapping, ApplyToChildren, Regenerate";
	private static final String UPSERT = """
		INSERT INTO FullData (
			DetailLevel, PosX, PosZ, MinY, DataChecksum,
			Data, ColumnGenerationStep, ColumnWorldCompressionMode, Mapping,
			NorthAdjData, SouthAdjData, EastAdjData, WestAdjData,
			DataFormatVersion, CompressionMode, ApplyToParent, ApplyToChildren, Regenerate,
			LastModifiedUnixDateTime, CreatedUnixDateTime)
		VALUES (0, ?, ?, 0, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, ?)
		ON CONFLICT(DetailLevel, PosX, PosZ) DO UPDATE SET
			DataChecksum = excluded.DataChecksum,
			Data = excluded.Data,
			ColumnGenerationStep = excluded.ColumnGenerationStep,
			ColumnWorldCompressionMode = excluded.ColumnWorldCompressionMode,
			Mapping = excluded.Mapping,
			NorthAdjData = excluded.NorthAdjData,
			SouthAdjData = excluded.SouthAdjData,
			EastAdjData = excluded.EastAdjData,
			WestAdjData = excluded.WestAdjData,
			DataFormatVersion = excluded.DataFormatVersion,
			CompressionMode = excluded.CompressionMode,
			ApplyToParent = excluded.ApplyToParent,
			ApplyToChildren = excluded.ApplyToChildren,
			Regenerate = excluded.Regenerate,
			LastModifiedUnixDateTime = excluded.LastModifiedUnixDateTime
		""";

	private final Connection connection;
	private @Nullable PreparedStatement read;
	private @Nullable PreparedStatement upsert;
	private int uncommitted;
	private byte format = DhSection.FORMAT_NO_BLOCK_MODELS;

	private DhDatabase(Connection connection) {
		this.connection = connection;
	}

	public static DhDatabase open(Path file, boolean write) throws SQLException, IOException {
		if (write) {
			Files.createDirectories(file.getParent());
		}
		DhDatabase database = new DhDatabase(connect(file.toAbsolutePath()));
		try {
			if (write) {
				database.updateSchema();
				database.connection.setAutoCommit(false);
			}
			return database;
		} catch (SQLException | IOException | RuntimeException e) {
			database.close();
			throw e;
		}
	}

	private static Connection connect(Path file) throws SQLException {
		return new JDBC().connect(JDBC.PREFIX + file, new Properties());
	}

	// The newest data format the installed Distant Horizons reads
	public byte format() {
		return this.format;
	}

	public boolean hasLods() throws SQLException {
		try (Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' AND name = 'FullData'")) {
			return result.next() && result.getInt(1) > 0;
		}
	}

	public int count() throws SQLException {
		try (Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM FullData WHERE DetailLevel = 0")) {
			return result.next() ? result.getInt(1) : 0;
		}
	}

	public LongSet positions() throws SQLException {
		LongSet positions = new LongOpenHashSet();
		try (Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT PosX, PosZ FROM FullData WHERE DetailLevel = 0")) {
			while (result.next()) {
				positions.add(pack(result.getInt(1), result.getInt(2)));
			}
		}
		return positions;
	}

	public static long pack(int x, int z) {
		return (long) x << 32 | z & 0xFFFFFFFFL;
	}

	public static int unpackX(long packed) {
		return (int) (packed >> 32);
	}

	public static int unpackZ(long packed) {
		return (int) packed;
	}

	// Stops once the consumer returns false
	public void forEachRow(Predicate<DhRow> consumer) throws SQLException {
		try (Statement statement = this.connection.createStatement();
			ResultSet result = statement.executeQuery("SELECT " + COLUMNS + " FROM FullData WHERE DetailLevel = 0")) {
			while (result.next()) {
				if (!consumer.test(row(result))) {
					return;
				}
			}
		}
	}

	public synchronized @Nullable DhRow read(int x, int z) throws SQLException {
		if (this.read == null) {
			this.read = this.connection.prepareStatement("SELECT " + COLUMNS + " FROM FullData WHERE DetailLevel = 0 AND PosX = ? AND PosZ = ?");
		}
		this.read.setInt(1, x);
		this.read.setInt(2, z);
		try (ResultSet result = this.read.executeQuery()) {
			return result.next() ? row(result) : null;
		}
	}

	public synchronized void write(DhRow row) throws SQLException {
		if (this.upsert == null) {
			this.upsert = this.connection.prepareStatement(UPSERT);
		}
		PreparedStatement upsert = this.upsert;
		long now = System.currentTimeMillis();
		int i = 1;
		upsert.setInt(i++, row.x());
		upsert.setInt(i++, row.z());
		upsert.setInt(i++, Arrays.hashCode(row.data()));
		upsert.setBytes(i++, row.data());
		upsert.setBytes(i++, row.generationSteps());
		upsert.setBytes(i++, row.worldCompression());
		upsert.setBytes(i++, row.mapping());
		upsert.setBytes(i++, row.north());
		upsert.setBytes(i++, row.south());
		upsert.setBytes(i++, row.east());
		upsert.setBytes(i++, row.west());
		upsert.setByte(i++, row.format());
		upsert.setByte(i++, row.compression());
		upsert.setBoolean(i++, row.applyToChildren());
		upsert.setBoolean(i++, row.regenerate());
		upsert.setLong(i++, now);
		upsert.setLong(i, now);
		upsert.executeUpdate();
		if (++this.uncommitted >= WRITES_PER_COMMIT) {
			this.connection.commit();
			this.uncommitted = 0;
		}
	}

	private static DhRow row(ResultSet result) throws SQLException {
		return new DhRow(
			result.getInt("PosX"),
			result.getInt("PosZ"),
			result.getByte("DataFormatVersion"),
			result.getByte("CompressionMode"),
			result.getBytes("Data"),
			result.getBytes("NorthAdjData"),
			result.getBytes("SouthAdjData"),
			result.getBytes("EastAdjData"),
			result.getBytes("WestAdjData"),
			result.getBytes("ColumnGenerationStep"),
			result.getBytes("ColumnWorldCompressionMode"),
			result.getBytes("Mapping"),
			result.getBoolean("ApplyToChildren"),
			result.getBoolean("Regenerate")
		);
	}

	// Runs Distant Horizons' own update scripts the way it does
	private void updateSchema() throws SQLException, IOException {
		try (Statement statement = this.connection.createStatement()) {
			statement.execute("""
				CREATE TABLE IF NOT EXISTS Schema (
					SchemaVersionId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
					ScriptName TEXT NOT NULL UNIQUE,
					AppliedDateTime DATETIME NOT NULL default CURRENT_TIMESTAMP
				)""");
		}
		for (String name : scriptNames()) {
			// Only versions with block model LODs ship this script
			if (name.contains("regenerateOldLods")) {
				this.format = DhSection.FORMAT_BLOCK_MODELS;
			}
			String path = SCRIPT_FOLDER + name;
			if (this.isApplied(path)) {
				continue;
			}
			String script = resource(path);
			boolean transaction = !script.contains("--No Transactions--");
			this.connection.setAutoCommit(!transaction);
			try (Statement statement = this.connection.createStatement()) {
				for (String part : script.split("--batch--")) {
					statement.execute(part);
				}
				if (transaction) {
					this.connection.commit();
				}
			} catch (SQLException e) {
				if (transaction) {
					this.connection.rollback();
				}
				throw e;
			} finally {
				this.connection.setAutoCommit(true);
			}
			try (PreparedStatement statement = this.connection.prepareStatement("INSERT INTO Schema (ScriptName) VALUES (?)")) {
				statement.setString(1, path);
				statement.executeUpdate();
			}
		}
	}

	private boolean isApplied(String path) throws SQLException {
		try (PreparedStatement statement = this.connection.prepareStatement("SELECT COUNT(*) FROM Schema WHERE ScriptName = ?")) {
			statement.setString(1, path);
			try (ResultSet result = statement.executeQuery()) {
				return result.next() && result.getInt(1) > 0;
			}
		}
	}

	private static List<String> scriptNames() throws IOException {
		List<String> names = new ArrayList<>();
		for (String line : resource(SCRIPT_FOLDER + "scriptList.txt").split("\n")) {
			if (!line.isBlank()) {
				names.add(line.trim());
			}
		}
		if (names.isEmpty()) {
			throw new IOException("Distant Horizons has no database scripts");
		}
		return names;
	}

	private static String resource(String path) throws IOException {
		try (InputStream in = DhDatabase.class.getClassLoader().getResourceAsStream(path)) {
			if (in == null) {
				throw new IOException("Missing " + path + " from Distant Horizons");
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	@Override
	public void close() throws SQLException {
		try {
			if (!this.connection.getAutoCommit()) {
				this.connection.commit();
			}
		} finally {
			this.connection.close();
		}
	}
}
