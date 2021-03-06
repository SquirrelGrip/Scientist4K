package com.github.squirrelgrip.scientist4k.http.core.comparator

import com.google.common.net.HttpHeaders

class DefaultHttpExperimentResponseComparator : HttpExperimentResponseComparator(
        StatusComparator(),
        HeadersComparator(
                HttpHeaders.SET_COOKIE,
                HttpHeaders.LAST_MODIFIED,
                HttpHeaders.DATE,
                HttpHeaders.CONTENT_LENGTH,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.EXPIRES,
                HttpHeaders.SERVER
        ),
        ContentComparator()
)

