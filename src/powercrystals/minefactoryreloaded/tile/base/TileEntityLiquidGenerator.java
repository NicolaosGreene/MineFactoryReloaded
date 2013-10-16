package powercrystals.minefactoryreloaded.tile.base;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import powercrystals.core.util.Util;
import powercrystals.minefactoryreloaded.core.ITankContainerBucketable;
import powercrystals.minefactoryreloaded.setup.Machine;

public abstract class TileEntityLiquidGenerator extends TileEntityGenerator implements ITankContainerBucketable
{
	private int _liquidConsumedPerTick;
	private int _powerProducedPerConsumption;
	private int _ticksBetweenConsumption;
	private int _outputPulseSize;
	
	private int _ticksSinceLastConsumption = 0;
	private int _bufferMax = 10000;
	private int _buffer;
	
	public TileEntityLiquidGenerator(Machine machine, int liquidConsumedPerTick, int powerProducedPerConsumption, int ticksBetweenConsumption)
	{
		super(machine);
		_liquidConsumedPerTick = liquidConsumedPerTick;
		_powerProducedPerConsumption = powerProducedPerConsumption;
		_ticksBetweenConsumption = ticksBetweenConsumption;
		_outputPulseSize = machine.getActivationEnergyMJ() * TileEntityFactoryPowered.energyPerMJ;
		
		_tank = new FluidTank(FluidContainerRegistry.BUCKET_VOLUME * 4);
	}
	
	protected abstract FluidStack getLiquidType();
	
	public int getBuffer()
	{
		return _buffer;
	}
	
	public void setBuffer(int buffer)
	{
		_buffer = buffer;
	}
	
	public int getBufferMax()
	{
		return _bufferMax;
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if(!worldObj.isRemote)
		{
			setIsActive(_buffer > _outputPulseSize * 2);
			
			int pulse = Math.min(_buffer, _outputPulseSize);
			_buffer -= pulse;
			_buffer += producePower(pulse);
			
			if (++_ticksSinceLastConsumption < _ticksBetweenConsumption)
				return;
			_ticksSinceLastConsumption = 0;
			
			if(Util.isRedstonePowered(this))
			{
				return;
			}
			
			if(_tank.getFluid() == null || _tank.getFluid().amount < _liquidConsumedPerTick || _bufferMax - _buffer < _powerProducedPerConsumption)
			{
				return;
			}
			
			_tank.drain(_liquidConsumedPerTick, true);
			_buffer += _powerProducedPerConsumption;
		}
	}
	
	@Override
	public boolean allowBucketFill()
	{
		return true;
	}
	
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (getLiquidType().isFluidEqual(resource))
			return _tank.fill(resource, doFill);
		return 0;
	}
	
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setInteger("ticksSinceLastConsumption", _ticksSinceLastConsumption);
		nbttagcompound.setInteger("buffer", _buffer);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		
		_ticksSinceLastConsumption = nbttagcompound.getInteger("ticksSinceLastConsumption");
		_buffer = nbttagcompound.getInteger("buffer");
	}
	
	@Override
	public boolean manageFluids()
	{
		return true;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}
}
