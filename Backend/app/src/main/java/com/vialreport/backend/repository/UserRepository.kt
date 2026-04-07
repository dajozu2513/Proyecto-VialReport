package com.vialreport.backend.repository

import com.vialreport.backend.model.User
import com.vialreport.backend.model.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class UserRepository {

    // Convierte una fila de la DB en un objeto User
    private fun rowToUser(row: ResultRow) = User(
        id = row[Users.id].value,
        name         = row[Users.name],
        email        = row[Users.email],
        passwordHash = row[Users.passwordHash],
        role         = row[Users.role],
        phone        = row[Users.phone],
        createdAt    = row[Users.createdAt]
    )

    fun findById(id: Int): User? {
        return Users
            .select { Users.id eq id }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    fun findByEmail(email: String): User? {
        return Users
            .select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    fun findAll(): List<User> {
        return Users
            .selectAll()
            .map { rowToUser(it) }
    }

    fun create(
        name: String,
        email: String,
        passwordHash: String,
        role: String,
        phone: String?
    ): User {
        val id = Users.insertAndGetId {
            it[Users.name]         = name
            it[Users.email]        = email
            it[Users.passwordHash] = passwordHash
            it[Users.role]         = role
            it[Users.phone]        = phone
        }
        return findById(id.value)!!
    }

    fun existsByEmail(email: String): Boolean {
        return Users
            .select { Users.email eq email }
            .count() > 0
    }

    fun update(id: Int, name: String, phone: String?): User? {
        Users.update({ Users.id eq id }) {
            it[Users.name]  = name
            it[Users.phone] = phone
        }
        return findById(id)
    }

    fun delete(id: Int): Boolean {
        return Users
            .deleteWhere { Users.id eq id } > 0
    }
}