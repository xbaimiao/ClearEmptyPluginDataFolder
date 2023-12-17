import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.InputStreamReader
import java.util.zip.ZipFile

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("确认删除") }

    var inputText by remember { mutableStateOf("E:\\MinecraftDev\\MinecraftServer\\group-lobby\\plugins") }
    var showDialog by remember { mutableStateOf(false) }
    var showDialogText by remember { mutableStateOf("") }

    // 警告对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("提示")
                }
            },
            text = {
                Text(showDialogText)
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                Text("清理的插件目录", modifier = Modifier.padding(end = 8.dp))
                OutlinedTextField(
                    inputText,
                    onValueChange = {
                        inputText = it
                        text = "确认删除"
                    }
                )
            }

            Button(onClick = {
                val folderPath = inputText
                println("确认按钮被点击，文件夹路径为: $folderPath")

                val folder = File(folderPath)
                if (!folder.exists()) {
                    showDialog = true
                    showDialogText = "文件夹不存在"
                    return@Button
                }

                val hasPlugins = ArrayList<String>()

                for (listFile in folder.listFiles()!!) {
                    if (listFile.name.endsWith(".jar")) {
                        ZipFile(listFile).use {
                            val config =
                                YamlConfiguration.loadConfiguration(InputStreamReader(it.getInputStream(it.getEntry("plugin.yml"))))
                            hasPlugins.add(config.getString("name")!!)
                        }
                    }
                }

                val files = ArrayList<File>()
                for (listFile in folder.listFiles()!!) {
                    if (listFile.isDirectory) {
                        if (listFile.name !in hasPlugins) {
                            files.add(listFile)
                        }
                    }
                }

                if (files.isEmpty()) {
                    showDialog = true
                    showDialogText = "没有待删除的文件夹"
                    return@Button
                }

                showDialog = true
                showDialogText = "删除文件夹 ${files.joinToString(",") { it.name }}"

                files.forEach {
                    deleteFolder(it)
                }

                text = "删除完成"
            }) {
                Text(text)
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "清理没有插件的文件夹") {
        App()
    }
}

fun deleteFolder(folder: File) {
    if (folder.isDirectory) {
        folder.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteFolder(file)
            } else {
                file.delete()
            }
        }
    }
    folder.delete()
}