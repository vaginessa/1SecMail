package viewModels

import Clipboard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import model.EmailBody
import model.EmailMessage
import networking.BASE_URL
import networking.httpClient
import utils.Utils.Logger
import utils.Utils.getDomainFromEmail
import utils.Utils.getNameFromEmail

class AppViewModel : ScreenModel {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> get() = _uiState
    var email = mutableStateOf("")
    private val _emailList = MutableStateFlow(mutableListOf<EmailBody>())
    val emailList: StateFlow<List<EmailBody>> get() = _emailList
    val cache by mutableStateOf(mutableMapOf<Int, EmailMessage>())
    private var job: Job? = null

    init {
        fetchMail()
    }

    override fun onDispose() {
        super.onDispose()
        httpClient.close()
        if (job != null && job?.isActive == true) job?.cancel()
    }

    private fun fetchMail() {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                _uiState.emit(UiState.Loading)
                getNewMail().getOrElse(0){"error,generate new mail"}.let {
                    email.value = it
                    _uiState.emit(UiState.Success(it))
                }
            } catch (e: Exception) {
                _uiState.emit(UiState.Error("Error loading data"))
            }
        }
    }

    private suspend fun getNewMail(): List<String> {
        kotlin.runCatching {
            val response = httpClient.get {
                url {
                    takeFrom(BASE_URL)
                    parameters.append("action", "genRandomMailbox")
                    parameters.append("count", "1")
                }
            }
            if (response.status == HttpStatusCode.OK) {
                if (job != null && job!!.isActive) job?.cancel("Already a email fetching job was running so closing this one.")
                job = repeatApiCall()
            }
            Logger("Email is ${response.body<List<String>>()[0]}")
            return response.body()
        }.onFailure {
            println("getNewMail ke time ${it.message}")
            return emptyList()
        }
        return emptyList()
    }

    private fun repeatApiCall(): Job {
        // Use the CoroutineScope to launch the coroutine
        val scope = CoroutineScope(Dispatchers.Default)
        return scope.launch {
            var isRunning = true

            while (isRunning) {
                // Perform your API call here (replace with your actual API call code)
                _emailList.emit(getEmailList().toMutableList())
                // Delay for 5 seconds before the next iteration
                delay(5000L)
                // Check if the coroutine should be stopped
                if (!isActive) {
                    isRunning = false
                }
            }
        }
    }

    private suspend fun getEmailList(): List<EmailBody> {
        kotlin.runCatching {
            val response = httpClient.get {
                url {
                    takeFrom(BASE_URL)
                    parameters.append("action", "getMessages")
                    parameters.append("login", email.value.getNameFromEmail())
                    parameters.append("domain", email.value.getDomainFromEmail())
                }
            }
            Logger("Email list is ${response.body<List<EmailBody>>()}")
            return response.body()
        }.onFailure {
            return emptyList()
        }
        return emptyList()
    }


    fun getDownloadUrl(id: String, file: String): String {
        return "$BASE_URL?action=download&login=${email.value.getNameFromEmail()}&domain=${email.value.getDomainFromEmail()}&id=$id&file=$file"
    }

    fun generateNewMail() {
        coroutineScope.launch(Dispatchers.IO) {
            getNewMail().getOrElse(0){"error,generate new mail"}.let {
                email.value = it
                _emailList.emit(mutableListOf())
            }
        }
    }

    fun copyMail(email: String) {
        Clipboard.copyTextToClipboard(email)
    }

    suspend fun openMail(id: Int): EmailMessage {
        return withContext(Dispatchers.IO) {
            getMailContent(id)
        }
    }

    private suspend fun getMailContent(id: Int): EmailMessage {
        kotlin.runCatching {
            val response = httpClient.get {
                url {
                    takeFrom(BASE_URL)
                    parameters.append("action", "readMessage")
                    parameters.append("login", email.value.getNameFromEmail())
                    parameters.append("domain", email.value.getDomainFromEmail())
                    parameters.append("id", id.toString())
                }
            }
            Logger("Email Content is ${response.body<EmailMessage>()}")
            return response.body()
        }.onFailure {
            println("getMailContent ke time ${it.message}")
            return EmailMessage()
        }
       return EmailMessage()
    }


}

sealed class UiState {
    object Loading : UiState()
    data class Success(val data: Any) : UiState()
    data class Error(val message: String) : UiState()
}

fun getFakeMailList() = listOf(
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        1,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        2,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        3,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        4,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        5,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        6,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        7,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        8,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        9,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
    EmailBody(
        "",
        "infokaydev@gmail.vom",
        10,
        "Method boolean androidx.compose.runtime.snapshots.SnapshotStateList.conditionalUpdate(kotlin.jvm.functions.Function1) failed lock verification"
    ),
)