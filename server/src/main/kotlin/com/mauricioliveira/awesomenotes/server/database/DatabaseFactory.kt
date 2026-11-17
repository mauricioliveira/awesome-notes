package com.mauricioliveira.awesomenotes.server.database

import com.mauricioliveira.awesomenotes.server.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {

    fun connect(config: DatabaseConfig): Database {
        val dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                maximumPoolSize = 10
            },
        )
        val database = Database.connect(datasource = dataSource)
        transaction(db = database) {
            SchemaUtils.create(UsersTable)
        }
        return database
    }
}