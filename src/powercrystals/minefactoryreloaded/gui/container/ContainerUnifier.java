package powercrystals.minefactoryreloaded.gui.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import powercrystals.minefactoryreloaded.gui.slot.SlotFake;
import powercrystals.minefactoryreloaded.gui.slot.SlotRemoveOnly;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

public class ContainerUnifier extends ContainerFactoryInventory
{
	public ContainerUnifier(TileEntityFactoryInventory tileentity, InventoryPlayer inv)
	{
		super(tileentity, inv);
	}
	
	@Override
	protected void addSlots()
	{
		addSlotToContainer(new Slot(_te, 0, 8, 23));
		addSlotToContainer(new SlotRemoveOnly(_te, 1, 8, 53));

		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 3; x++)
			{
				addSlotToContainer(new SlotFake(_te, y * 3 + x + 2, 50 + x * 18, 23 + y * 18));
			}
		}
	}
	
	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		for (int i = 2; i --> 0; )
		{
			Slot slotObject = (Slot)inventorySlots.get(i);
			if (slotObject != null)
				for (int j = 0; j < this.crafters.size(); ++j)
					((ICrafting)this.crafters.get(j)).
						sendSlotContents(this, slotObject.slotNumber, slotObject.getStack());
		}
	}
}
