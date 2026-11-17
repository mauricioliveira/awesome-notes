package com.mauricioliveira.awesomenotes.server.config

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
)

data class AppConfig(
    val port: Int,
    val database: DatabaseConfig,
) {
    companion object {
        fun fromEnv(env: Map<String, String> = System.getenv()): AppConfig = AppConfig(
            port = env["PORT"]?.toIntOrNull() ?: 8080,
            database = DatabaseConfig(
                url = env.required("DATABASE_URL"),
                user = env.required("DATABASE_USER"),
                password = env.required("DATABASE_PASSWORD"),
            ),
        )

        private fun Map<String, String>.required(key: String): String =
            this[key] ?: error("Missing required environment variable: $key")
    }
}