package com.mauricioliveira.awesomenotes.server.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

suspend fun <T> dbQuery(database: Database, block: () -> T): T =
    withContext(Dispatchers.IO) {
        transaction(database) { block() }
    }
