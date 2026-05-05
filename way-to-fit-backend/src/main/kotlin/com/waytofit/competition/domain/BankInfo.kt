package com.waytofit.competition.domain

import jakarta.persistence.Embeddable

@Embeddable
data class BankInfo(
    val bankName: String,
    val accountNumber: String,
    val accountHolder: String,
    val entryFee: Int = 0,
) {
    companion object {
        fun empty() = BankInfo("", "", "", 0)
    }
}
