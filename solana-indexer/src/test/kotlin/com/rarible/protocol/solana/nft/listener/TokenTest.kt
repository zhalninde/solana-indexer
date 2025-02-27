package com.rarible.protocol.solana.nft.listener

import com.rarible.core.test.wait.Wait
import com.rarible.protocol.solana.common.meta.TokenMeta
import com.rarible.protocol.solana.common.model.Balance
import com.rarible.protocol.solana.common.model.Token
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.Instant
import java.util.*

class TokenTest : EventAwareBlockScannerTest() {

    // In this test, the token meta is empty.
    private val emptyTokenMeta: TokenMeta? = null

    @Test
    fun `mint, burn, transfer token`() = runBlocking {
        val decimals = 3
        val aliceWallet = createWallet("${UUID.randomUUID()}")
        val tokenAddress = createToken(decimals)
        val account = createAccount(tokenAddress)
        val aliceAccount = createAccount(tokenAddress, aliceWallet)

        mintToken(tokenAddress, amount = 5UL)
        val token = Token(
            mint = tokenAddress,
            hasMeta = false,
            supply = 5.scaleSupply(decimals),
            isDeleted = false,
            decimals = decimals,
            revertableEvents = emptyList(),
            createdAt = Instant.EPOCH, // TODO[tests]: consider fetching from the blockchain.
            updatedAt = Instant.EPOCH
        )
        val fromBalance = Balance(
            account = account,
            owner = getWallet(),
            mint = tokenAddress,
            value = 5.scaleSupply(decimals),
            revertableEvents = emptyList(),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )
        val aliceBalance = Balance(
            account = aliceAccount,
            owner = aliceWallet,
            mint = tokenAddress,
            value = BigInteger.ZERO,
            revertableEvents = emptyList(),
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
        )

        Wait.waitAssert {
            assertToken(token)
            assertUpdateTokenEvent(token, emptyTokenMeta)
            assertUpdateBalanceEvent(fromBalance, emptyTokenMeta)
            assertBalance(fromBalance)
        }

        burnToken(account, amount = 4UL)
        Wait.waitAssert {
            val partlyBurnedToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(partlyBurnedToken)
            assertUpdateTokenEvent(partlyBurnedToken, emptyTokenMeta)

            val partlyBurnedBalance = fromBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(partlyBurnedBalance)
            assertUpdateBalanceEvent(partlyBurnedBalance, emptyTokenMeta)
        }

        transferToken(tokenAddress, amount = 1UL, aliceAccount)
        Wait.waitAssert {
            val finalToken = token.copy(supply = 1.scaleSupply(decimals))
            assertToken(finalToken)
            assertUpdateTokenEvent(finalToken, emptyTokenMeta)

            val finalFromBalance = fromBalance.copy(value = 0.scaleSupply(decimals))
            assertBalance(finalFromBalance)
            assertUpdateBalanceEvent(finalFromBalance, emptyTokenMeta)

            val finalAliceBalance = aliceBalance.copy(value = 1.scaleSupply(decimals))
            assertBalance(finalAliceBalance)
            assertUpdateBalanceEvent(finalAliceBalance, emptyTokenMeta)
        }
    }
}
