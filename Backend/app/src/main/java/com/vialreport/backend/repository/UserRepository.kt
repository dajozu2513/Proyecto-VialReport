package com.vialreport.backend.repository

import com.vialreport.backend.model.User
import com.vialreport.backend.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    private fun rowToUser(row: ResultRow) = User(
        id           = row[Users.id].value,
        name         = row[Users.name],
        email        = row[Users.email],
        passwordHash = row[Users.passwordHash],
        role         = row[Users.role],
        phone        = row[Users.phone],
        cedula       = row[Users.cedula],
        isVerified   = row[Users.isVerified],
        createdAt    = row[Users.createdAt]
    )

    fun findById(id: Int): User? = transaction {
        Users.select { Users.id eq id }.map { rowToUser(it) }.singleOrNull()
    }

    fun findByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }.map { rowToUser(it) }.singleOrNull()
    }

    fun findAll(): List<User> = transaction {
        Users.selectAll().map { rowToUser(it) }
    }

    fun create(
        name: String,
        email: String,
        passwordHash: String,
        role: String,
        phone: String?,
        cedula: String? = null
    ): User = transaction {
        val id = Users.insertAndGetId {
            it[Users.name]         = name
            it[Users.email]        = email
            it[Users.passwordHash] = passwordHash
            it[Users.role]         = role
            it[Users.phone]        = phone
            it[Users.cedula]       = cedula
        }
        Users.select { Users.id eq id }.map { rowToUser(it) }.single()
    }

    fun existsByEmail(email: String): Boolean = transaction {
        Users.select { Users.email eq email }.count() > 0
    }

    fun update(id: Int, name: String, phone: String?): User? = transaction {
        Users.update({ Users.id eq id }) {
            it[Users.name]  = name
            it[Users.phone] = phone
        }
        Users.select { Users.id eq id }.map { rowToUser(it) }.singleOrNull()
    }

    fun delete(id: Int): Boolean = transaction {
        Users.deleteWhere { Users.id eq id } > 0
    }
}
