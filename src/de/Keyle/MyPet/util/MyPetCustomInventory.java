/*
 * Copyright (C) 2011-2012 Keyle
 *
 * This file is part of MyPet
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyPet. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.util;

import de.Keyle.MyPet.skill.skills.Inventory;
import net.minecraft.server.*;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class MyPetCustomInventory implements IInventory
{
    private String InventroyName;
    private List<ItemStack> items = new ArrayList<ItemStack>();
    private int Size = 0;
    private int stackSize = 64;
    private List<HumanEntity> transaction = new ArrayList<HumanEntity>();

    public MyPetCustomInventory(String Name, int Size)
    {
        setName(Name);
        setSize(Size);
    }

    public int getSize()
    {
        return items.size();
    }

    public void setSize(int Size)
    {
        this.Size = Size;
        for (int i = items.size() ; i < Size ; i++)
        {
            items.add(i, null);
        }
    }

    public String getName()
    {
        return InventroyName;
    }

    public void setName(String Name)
    {
        if (Name.length() > 16)
        {
            Name = Name.substring(0, 16);
        }
        this.InventroyName = Name;
    }

    public ItemStack getItem(int i)
    {
        if (i <= Size)
        {
            return items.get(i);
        }
        return null;
    }

    public void setItem(int i, ItemStack itemStack)
    {
        if (i < items.size())
        {
            items.set(i, itemStack);
        }
        else
        {
            for (int x = items.size() ; x < i ; x++)
            {
                items.add(x, null);
            }
            items.add(i, itemStack);
        }
        update();
    }

    public int addItem(org.bukkit.inventory.ItemStack item)
    {
        item = item.clone();
        int ItemID = item.getTypeId();
        int ItemDuarbility = item.getDurability();
        int ItemMaxStack = item.getMaxStackSize();
        for (int i = 0 ; i < this.getSize() ; i++)
        {
            if (getItem(i) != null && getItem(i).id == ItemID && getItem(i).getData() == ItemDuarbility && getItem(i).getEnchantments() == null && item.getEnchantments().size() == 0 && getItem(i).count < ItemMaxStack)
            {
                if (item.getAmount() >= ItemMaxStack - getItem(i).count)
                {
                    item.setAmount(item.getAmount() - (ItemMaxStack - getItem(i).count));
                    getItem(i).count = ItemMaxStack;
                }
                else
                {
                    getItem(i).count += item.getAmount();
                    item.setAmount(0);
                    break;
                }
            }
        }
        for (int i = 0 ; i < getSize() ; i++)
        {
            if (item.getAmount() <= 0)
            {
                break;
            }
            if (getItem(i) == null)
            {
                if (item.getAmount() <= ItemMaxStack)
                {
                    setItem(i, ((CraftItemStack) item.clone()).getHandle());
                    item.setAmount(0);
                    break;
                }
                else
                {
                    org.bukkit.inventory.ItemStack is = item.clone();
                    is.setAmount(is.getMaxStackSize());
                    setItem(i, ((CraftItemStack) is).getHandle());
                    item.setAmount(item.getAmount() - is.getMaxStackSize());
                }
            }
        }
        update();
        return item.getAmount();
    }

    public ItemStack splitStack(int i, int j)
    {
        if (i <= Size && items.get(i) != null)
        {
            ItemStack itemstack;
            if (items.get(i).count <= j)
            {
                itemstack = items.get(i);
                items.set(i, null);
                return itemstack;
            }
            else
            {
                itemstack = items.get(i).a(j);
                if (items.get(i).count == 0)
                {
                    items.set(i, null);
                }
                return itemstack;
            }
        }
        return null;
    }

    public ItemStack[] getContents()
    {
        ItemStack[] itemStack = new ItemStack[getSize()];
        for (int i = 0 ; i < getSize() ; i++)
        {
            itemStack[i] = items.get(i);
        }
        return itemStack;
    }

    public NBTTagCompound save(NBTTagCompound nbtTagCompound)
    {
        NBTTagList Items = new NBTTagList();
        for (int i = 0 ; i < this.items.size() ; i++)
        {
            ItemStack itemStack = this.items.get(i);
            if (itemStack != null)
            {
                NBTTagCompound Item = new NBTTagCompound();
                Item.setByte("Slot", (byte) i);
                itemStack.save(Item);
                Items.add(Item);
            }
        }
        nbtTagCompound.set("Items", Items);
        return nbtTagCompound;
    }

    public void load(NBTTagCompound nbtTagCompound)
    {
        NBTTagList Items = nbtTagCompound.getList("Items");

        for (int i = 0 ; i < Items.size() ; i++)
        {
            NBTTagCompound Item = (NBTTagCompound) Items.get(i);

            ItemStack itemStack = ItemStack.a(Item);
            setItem(Item.getByte("Slot"), itemStack);
        }
    }

    public boolean a(EntityHuman entityHuman)
    {
        return true;
    }

    public void startOpen()
    {
    }

    public void onOpen(CraftHumanEntity who)
    {
        this.transaction.add(who);
    }

    public void onClose(CraftHumanEntity who)
    {
        this.transaction.remove(who);
        Player player = MyPetUtil.getServer().getPlayer(who.getName());
        if (MyPetList.hasMyPet(player))
        {
            if (Inventory.PetChestOpened.contains(player.getPlayer()))
            {
                MyPetList.getMyPet(player).setSitting(false);
                Inventory.PetChestOpened.remove(player.getPlayer());
            }
        }
    }

    public List<HumanEntity> getViewers()
    {
        return this.transaction;
    }

    public InventoryHolder getOwner()
    {
        return null;
    }

    public int getMaxStackSize()
    {
        return stackSize;
    }

    public void setMaxStackSize(int i)
    {
        this.stackSize = i;
    }

    public ItemStack splitWithoutUpdate(int i)
    {
        if (items.get(i) != null)
        {
            ItemStack itemstack = items.get(i);

            items.set(i, null);
            return itemstack;
        }
        return null;
    }

    public void update()
    {
    }

    public void f()
    {
    }
}