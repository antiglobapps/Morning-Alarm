package com.morningalarm.server.shared.persistence

interface TransactionRunner {
    fun <T> inTransaction(block: () -> T): T
}
