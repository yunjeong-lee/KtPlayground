import docGenerator.generateDocs

import utils.currentPath
import utils.getProjectKtFiles
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

fun main() {
    generateDocs()
    copyFiles()
}

fun copyFiles() {
    println("Copying files")
    getProjectKtFiles().forEach { file ->
        Files.copy(
            file.toPath(),
            Paths.get("$currentPath/flat-files/${file.name}"),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}