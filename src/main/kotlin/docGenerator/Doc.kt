package docGenerator

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.testFramework.LightVirtualFile
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import utils.asString
import utils.currentPath
import utils.getProjectKtFiles
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun generateDocs() {
    println("Generating docs")
    val files: List<File> = getProjectKtFiles()
    files.forEach { file: File ->
        val codeString = file.asString()
        val ktFile = createKtFile(codeString, file.name)
        val functions = ktFile.getFunctionList()
        functions.joinToString { ktFunction ->
            val doc = ktFunction.docComment
            doc?.let {
                val functionDocText = ktFunction.text
                return@joinToString "$functionDocText \n \n"
            } ?: return@joinToString ""
        }.takeIf { functionDocTexts ->
            functionDocTexts.isNotEmpty()
        }.also {
            val doc = "\n# ${file.name} \n ```kotlin \n ${it?.trimEnd()} \n ```"
            val readMePath = "$currentPath/doc/${file.name.replace(".kt", "")}.md"
            Files.write(
                Paths.get(readMePath),
                doc.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }
}

//private val proj by lazy {
//    KotlinCoreEnvironment.createForProduction(
//        Disposer.newDisposable(),
//        CompilerConfiguration(),
//        EnvironmentConfigFiles.JVM_CONFIG_FILES
//    ).project
//}
//
//fun createKtFile(codeString: String, fileName: String): KtFile {
//    val fileType: FileType = KotlinFileType.INSTANCE as FileType
//    val file = LightVirtualFile(fileName, fileType, codeString)
//    return PsiManager.getInstance(proj).findFile(file) as KtFile
//}

fun createKtFile(codeString: String, fileName: String): KtFile {
    val disposable: Disposable = Disposer.newDisposable()
    val config = CompilerConfiguration()
    config.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, PrintingMessageCollector(System.err, MessageRenderer.PLAIN_FULL_PATHS, false))
    val configFiles: EnvironmentConfigFiles = EnvironmentConfigFiles.JVM_CONFIG_FILES
    try {
        val env =
            KotlinCoreEnvironment.createForProduction(disposable, config, configFiles)
        val fileType: FileType = KotlinFileType.INSTANCE as FileType
        val file = LightVirtualFile(fileName, fileType, codeString.trimIndent())
        val res: KtFile = PsiManager.getInstance(env.project).findFile(file) as KtFile // env.getSourceFiles().first()
        return res
    } finally {
        disposable.dispose()
    }
}
