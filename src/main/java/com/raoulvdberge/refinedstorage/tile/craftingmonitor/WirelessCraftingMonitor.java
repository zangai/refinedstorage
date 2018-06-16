package com.raoulvdberge.refinedstorage.tile.craftingmonitor;

import com.google.common.base.Optional;
import com.raoulvdberge.refinedstorage.RS;
import com.raoulvdberge.refinedstorage.api.autocrafting.ICraftingManager;
import com.raoulvdberge.refinedstorage.api.autocrafting.task.ICraftingTask;
import com.raoulvdberge.refinedstorage.api.network.INetwork;
import com.raoulvdberge.refinedstorage.gui.GuiBase;
import com.raoulvdberge.refinedstorage.gui.GuiCraftingMonitor;
import com.raoulvdberge.refinedstorage.item.ItemWirelessCraftingMonitor;
import com.raoulvdberge.refinedstorage.network.MessageWirelessCraftingMonitorSettings;
import com.raoulvdberge.refinedstorage.tile.data.TileDataParameter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class WirelessCraftingMonitor implements ICraftingMonitor {
    private ItemStack stack;

    private int networkDimension;
    private BlockPos network;
    private int size;
    private int tabPage;
    private Optional<UUID> tabSelected;

    public WirelessCraftingMonitor(int networkDimension, ItemStack stack) {
        this.stack = stack;
        this.networkDimension = networkDimension;
        this.network = new BlockPos(ItemWirelessCraftingMonitor.getX(stack), ItemWirelessCraftingMonitor.getY(stack), ItemWirelessCraftingMonitor.getZ(stack));
        this.size = ItemWirelessCraftingMonitor.getSize(stack);
        this.tabPage = ItemWirelessCraftingMonitor.getTabPage(stack);
        this.tabSelected = ItemWirelessCraftingMonitor.getTabSelected(stack);
    }

    @Override
    public String getGuiTitle() {
        return "gui.refinedstorage:wireless_crafting_monitor";
    }

    @Override
    public void onCancelled(EntityPlayerMP player, UUID id) {
        INetwork network = getNetwork();

        if (network != null) {
            network.getItemGridHandler().onCraftingCancelRequested(player, id);
        }
    }

    @Override
    public TileDataParameter<Integer, ?> getRedstoneModeParameter() {
        return null;
    }

    @Override
    public Collection<ICraftingTask> getTasks() {
        INetwork network = getNetwork();

        if (network != null) {
            return network.getCraftingManager().getTasks();
        }

        return Collections.emptyList();
    }

    @Nullable
    @Override
    public ICraftingManager getCraftingManager() {
        INetwork network = getNetwork();

        if (network != null) {
            return network.getCraftingManager();
        }

        return null;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void onSizeChanged(int size) {
        this.size = size;

        GuiBase.executeLater(GuiCraftingMonitor.class, GuiBase::initGui);

        RS.INSTANCE.network.sendToServer(new MessageWirelessCraftingMonitorSettings(size, tabSelected, tabPage));
    }

    private INetwork getNetwork() {
        World world = DimensionManager.getWorld(networkDimension);

        if (world != null) {
            TileEntity tile = world.getTileEntity(network);

            return tile instanceof INetwork ? (INetwork) tile : null;
        }

        return null;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onClosed(EntityPlayer player) {
        INetwork network = getNetwork();

        if (network != null) {
            network.getNetworkItemHandler().onClose(player);
        }
    }

    @Override
    public Optional<UUID> getTabSelected() {
        return tabSelected;
    }

    @Override
    public int getTabPage() {
        return tabPage;
    }

    @Override
    public void onTabSelectionChanged(Optional<UUID> taskId) {
        if (taskId.isPresent() && tabSelected.isPresent() && taskId.get().equals(tabSelected.get())) {
            this.tabSelected = Optional.absent();
        } else {
            this.tabSelected = taskId;
        }

        RS.INSTANCE.network.sendToServer(new MessageWirelessCraftingMonitorSettings(size, tabSelected, tabPage));
    }

    @Override
    public void onTabPageChanged(int page) {
        if (page >= 0) {
            this.tabPage = page;

            RS.INSTANCE.network.sendToServer(new MessageWirelessCraftingMonitorSettings(size, tabSelected, tabPage));
        }
    }
}
