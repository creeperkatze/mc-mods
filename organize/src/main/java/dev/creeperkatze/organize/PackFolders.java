package dev.creeperkatze.organize;

import dev.creeperkatze.organize.mixin.AbstractSelectionListAccessor;
import dev.creeperkatze.organize.mixin.PackSelectionScreenAccessor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

// Resource packs in subfolders of the resource pack folder get the folder path in their id, like file/Faithful/x.zip
public final class PackFolders {
	private static final String FILE_PREFIX = "file/";
	private static final int MAX_DEPTH = 8;
	private static final int ENTRY_HEIGHT = 36;
	private static final Comparator<String> NAME_ORDER = String.CASE_INSENSITIVE_ORDER.thenComparing(Comparator.naturalOrder());

	private PackFolders() {
	}

	// Calls discover for every folder below root that isn't a pack itself
	public static void discover(Path root, Consumer<Path> discover) {
		discover(root, discover, 1);
	}

	private static void discover(Path folder, Consumer<Path> discover, int depth) {
		if (depth > MAX_DEPTH) {
			return;
		}
		try (DirectoryStream<Path> contents = Files.newDirectoryStream(folder)) {
			for (Path content : contents) {
				if (Files.isDirectory(content, LinkOption.NOFOLLOW_LINKS) && !Files.exists(content.resolve("pack.mcmeta"))) {
					discover.accept(content);
					discover(content, discover, depth + 1);
				}
			}
		} catch (IOException e) {
			Organize.LOGGER.warn("Failed to list pack folders in {}", folder, e);
		}
	}

	public static PackLocationInfo locationInfo(PackLocationInfo info, Path root, Path content) {
		String path = root.relativize(content).toString().replace('\\', '/');
		if (path.equals(content.getFileName().toString())) {
			return info;
		}
		return new PackLocationInfo(FILE_PREFIX + path, info.title(), info.source(), info.knownPackInfo());
	}

	// Shows the packs of the open folder in the available list, with its subfolders above them
	public static void show(PackSelectionScreen screen, TransferableSelectionList list, String search, Stream<PackSelectionModel.Entry> entries, Consumer<Stream<PackSelectionModel.Entry>> update) {
		if (list != ((PackSelectionScreenAccessor) screen).organize$getAvailablePackList() || !search.isBlank()) {
			update.accept(entries);
			return;
		}
		String folder = ((PackFolderScreen) screen).organize$getFolder();
		List<PackSelectionModel.Entry> packs = entries.toList();
		update.accept(packs.stream().filter(pack -> folderOf(pack).equals(folder)));

		// Every pack below each subfolder, in the order of the list
		Map<String, List<PackSelectionModel.Entry>> subfolders = new TreeMap<>(NAME_ORDER);
		for (PackSelectionModel.Entry pack : packs) {
			String packFolder = folderOf(pack);
			if (isInside(packFolder, folder)) {
				subfolders.computeIfAbsent(childName(packFolder, folder), name -> new ArrayList<>()).add(pack);
			}
		}
		Path packDir = ((PackSelectionScreenAccessor) screen).organize$getPackDir();
		List<LayoutElement> rows = ((AbstractSelectionListAccessor) list).organize$getChildren();
		int index = 1;
		if (!folder.isEmpty()) {
			add(rows, index++, PackFolderEntry.back(list, screen, parent(folder)));
		}
		for (Map.Entry<String, List<PackSelectionModel.Entry>> subfolder : subfolders.entrySet()) {
			String path = join(folder, subfolder.getKey());
			Identifier icon = PackFolderIcons.get(packDir.resolve(path));
			add(rows, index++, PackFolderEntry.folder(list, screen, path, subfolder.getKey(), icon, subfolder.getValue()));
		}
		((AbstractSelectionListAccessor) list).organize$repositionEntries();
		list.refreshScrollAmount();
	}

	private static void add(List<LayoutElement> rows, int index, PackFolderEntry entry) {
		entry.setHeight(ENTRY_HEIGHT);
		rows.add(index, entry);
	}

	public static void open(PackSelectionScreen screen, String folder) {
		((PackFolderScreen) screen).organize$setFolder(folder);
		PackSelectionScreenAccessor accessor = (PackSelectionScreenAccessor) screen;
		accessor.organize$populateLists(null);
		TransferableSelectionList available = accessor.organize$getAvailablePackList();
		if (available != null) {
			available.setScrollAmount(0.0);
		}
	}

	public static String folderOf(PackSelectionModel.Entry pack) {
		String id = pack.getId();
		int slash = id.lastIndexOf('/');
		return id.startsWith(FILE_PREFIX) && slash > FILE_PREFIX.length() ? id.substring(FILE_PREFIX.length(), slash) : "";
	}

	private static boolean isInside(String folder, String parent) {
		return parent.isEmpty() ? !folder.isEmpty() : folder.startsWith(parent + "/");
	}

	private static String childName(String folder, String parent) {
		String rest = parent.isEmpty() ? folder : folder.substring(parent.length() + 1);
		int slash = rest.indexOf('/');
		return slash < 0 ? rest : rest.substring(0, slash);
	}

	private static String parent(String folder) {
		int slash = folder.lastIndexOf('/');
		return slash < 0 ? "" : folder.substring(0, slash);
	}

	private static String join(String parent, String name) {
		return parent.isEmpty() ? name : parent + "/" + name;
	}
}
