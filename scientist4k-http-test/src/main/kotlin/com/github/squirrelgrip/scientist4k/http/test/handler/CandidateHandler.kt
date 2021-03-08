package com.github.squirrelgrip.scientist4k.http.test.handler

import com.github.squirrelgrip.extension.json.toJson
import com.github.squirrelgrip.scientist4k.http.core.configuration.ConnectorConfiguration
import com.github.squirrelgrip.scientist4k.http.core.configuration.ServerConfiguration
import com.github.squirrelgrip.scientist4k.http.core.server.SecuredServer
import com.github.squirrelgrip.scientist4k.http.test.handler.CommonUtil.helloContent
import com.github.squirrelgrip.scientist4k.http.test.handler.CommonUtil.textHtmlContentType
import com.github.squirrelgrip.scientist4k.http.test.handler.CommonUtil.textPlainContentType
import com.google.common.net.MediaType
import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CandidateHandler : AbstractHandler() {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CandidateHandler::class.java)

        val serverConfiguration = ServerConfiguration(
            listOf(
                ConnectorConfiguration(9011),
                ConnectorConfiguration(9012, Main.sslConfiguration)
            )
        )

        fun handleRequest(request: HttpServletRequest, response: HttpServletResponse, target: String) {
            LOGGER.info("CandidateHandler received request: ${request.method} ${request.requestURL}")
            val out = response.writer
            when (target) {
                "/candidate" -> {
                    response.contentType = textPlainContentType
                    response.status = HttpServletResponse.SC_OK
                    out.println("Candidate")
                }
                "/ok" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_OK
                    out.println("<h1>OK</h1>")
                }
                "/differentContent" -> {
                    response.contentType = textPlainContentType
                    response.status = HttpServletResponse.SC_OK
                    out.println("Candidate Content")
                }
                "/mappedCandidate" -> {
                    response.contentType = textPlainContentType
                    response.status = HttpServletResponse.SC_OK
                    out.println("mapped")
                }
                "/mappedCandidateDifferent" -> {
                    response.contentType = textPlainContentType
                    response.status = HttpServletResponse.SC_CREATED
                    out.println("mappedDifferentCandidate")
                }
                "/status" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_CREATED
                    out.println("<h1>status</h1>")
                }
                "/contentType" -> {
                    response.contentType = "text/html"
                    response.status = HttpServletResponse.SC_CREATED
                    out.println("<h1>content type</h1>")
                }
                "/cookie" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_OK
                    response.addCookie(Cookie("name", "value"))
                    out.println("<h1>Cookie</h1>")
                }
                "/addcookie" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_OK
                    response.addCookie(Cookie("name", "value"))
                    response.addCookie(Cookie("add", "value"))
                    out.println(helloContent)
                }
                "/alteredcookie" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_OK
                    response.addCookie(Cookie("name", "different_value"))
                    out.println(helloContent)
                }
                "/removedcookie" -> {
                    response.contentType = textHtmlContentType
                    response.status = HttpServletResponse.SC_OK
                    out.println(helloContent)
                }
                "/redirect" -> {
                    response.sendRedirect("/ok")
                }
                "/json" -> {
                    response.contentType = MediaType.JSON_UTF_8.toString()
                    response.status = HttpServletResponse.SC_OK
                    out.println(
                        mapOf(
                            "1" to "AAA",
                            "2" to listOf("BBB", "CCC"),
                            "3" to mapOf("4" to listOf("DDD", "EEE"))
                        ).toJson()
                    )
                }
                "/jsonDifferent" -> {
                    response.contentType = MediaType.JSON_UTF_8.toString()
                    response.status = HttpServletResponse.SC_OK
                    out.println(
                        mapOf(
                            "5" to "AAA",
                            "2" to listOf("BBB", "CCC"),
                            "3" to mapOf("4" to listOf("DDD", "EEE"))
                        ).toJson()
                    )
                }
                else -> {
                    response.status = HttpServletResponse.SC_NOT_FOUND
                }
            }
        }

    }

    override fun handle(
        target: String,
        baseRequest: Request,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        handleRequest(request, response, target)
        baseRequest.isHandled = true
    }


}

fun main() {
    val server = SecuredServer(CandidateHandler.serverConfiguration, CandidateHandler())
    server.start()
    server.join()
}
