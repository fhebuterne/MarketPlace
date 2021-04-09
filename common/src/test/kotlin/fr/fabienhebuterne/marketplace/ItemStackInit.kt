package fr.fabienhebuterne.marketplace

import io.mockk.every
import io.mockk.mockk
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

fun initMock(
    material: Material,
    amount: Int = 1,
    similar: ItemStack?,
    isSimilar: Boolean,
    maxStackSize: Int = 64
): ItemStack {
    val itemStack: ItemStack = mockk()
    every { itemStack.type } returns material
    every { itemStack.amount } returns amount
    every { itemStack.isSimilar(similar) } returns isSimilar
    every { itemStack.clone() } returns itemStack
    every { itemStack.maxStackSize } returns maxStackSize
    return itemStack
}