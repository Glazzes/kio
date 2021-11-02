package com.kio.configuration.pg

import org.hibernate.dialect.PostgreSQL10Dialect
import java.sql.Types

class CustomDialect : PostgreSQL10Dialect() {
    init {
        registerColumnType(Types.BLOB, "bytea")
    }
}