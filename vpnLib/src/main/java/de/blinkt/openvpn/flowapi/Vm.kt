package de.blinkt.openvpn.flowapi


import androidx.appcompat.app.AppCompatActivity
import com.mucc.flownet.HandlerException
import de.blinkt.openvpn.model.BaseResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import okhttp3.ResponseBody
import java.util.concurrent.CancellationException

/**
 * 使用flow+retrofit
 * @param request [@kotlin.ExtensionFunctionType] SuspendFunction1<ApiInterface, BaseResponse<T>?>
 * @return Flow<BaseResponse<T>>
 */
suspend fun <T> AppCompatActivity.flowRequest(
    request: suspend ApiInterface.() -> BaseResponse<T>?
): Flow<BaseResponse<T>> {
    return flow {
        val response = request(Api) ?: throw IllegalArgumentException("数据非法，获取响应数据为空")
        response.throwAPIException();
        emit(response)
    }.flowOn(Dispatchers.IO)
        .onCompletion { cause ->
            run {
                cause?.let { throw catchException( it) }// 这里再重新把捕获的异常再次抛出，调用的时候如果有必要可以再次catch 获取异常
            }
        }
}

suspend fun AppCompatActivity.flowRequestDown(
    request: suspend ApiInterface.() -> ResponseBody
): Flow<ResponseBody> {
    return flow {
        val response = request(Api)
        emit(response)
    }.flowOn(Dispatchers.IO)
        .onCompletion { cause ->
            run {
                cause?.let { throw catchException( it) }// 这里再重新把捕获的异常再次抛出，调用的时候如果有必要可以再次catch 获取异常
            }
        }
}

fun catchException(e: Throwable, ): Throwable {
    e.printStackTrace()
    if (e is CancellationException) {
        return e;
    }
    val exception = HandlerException.handlerException(e)
    return exception;
}
fun <T> Flow<T>.catchError(bloc: Throwable.() -> Unit) = catch { cause -> bloc(cause) }

suspend fun <T> Flow<T>.next(bloc: suspend T.() -> Unit): Unit = catch { }.collect { bloc(it) }
