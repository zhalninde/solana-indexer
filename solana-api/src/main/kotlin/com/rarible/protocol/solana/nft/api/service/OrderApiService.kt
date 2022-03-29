package com.rarible.protocol.solana.nft.api.service

import com.rarible.protocol.solana.common.model.Order
import com.rarible.protocol.solana.common.model.OrderId
import com.rarible.protocol.solana.common.model.order.filter.OrderFilter
import com.rarible.protocol.solana.common.repository.OrderRepository
import com.rarible.protocol.solana.nft.api.exceptions.EntityNotFoundApiException
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class OrderApiService(
    private val orderRepository: OrderRepository
) {
    suspend fun getOrderById(id: String): Order =
        orderRepository.findById(id) ?: throw EntityNotFoundApiException("Order", id)

    suspend fun findByIds(ids: List<OrderId>): List<Order> =
        orderRepository.findByIds(ids).toList()

    suspend fun getOrders(
        orderFilter: OrderFilter,
        size: Int
    ): List<Order> {
        val query = orderFilter.getQuery(size)
        val orders = when (orderFilter) {
            is OrderFilter.All -> orderRepository.query(query).toList()
        }
        return orders
    }
}
