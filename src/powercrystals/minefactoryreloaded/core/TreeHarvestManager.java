package powercrystals.minefactoryreloaded.core;

import java.util.Map;

import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import powercrystals.core.position.Area;
import powercrystals.core.position.BlockPosition;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.HarvestType;
import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
import powercrystals.minefactoryreloaded.core.BlockPool.BlockNode;

public class TreeHarvestManager implements IHarvestManager
{
	private static ForgeDirection[] SIDES = {ForgeDirection.EAST, ForgeDirection.WEST,
		ForgeDirection.SOUTH, ForgeDirection.NORTH};
	private BlockPool _treeBlocks;
	private boolean _isDone;

	private HarvestMode _harvestMode;
	private Area _treeArea;
	private World _world;

	public TreeHarvestManager(World world, Area treeArea, HarvestMode harvestMode)
	{
		reset(world, treeArea, harvestMode);
	}

	@Override
	public BlockPosition getNextBlock()
	{
		BlockNode bn = _treeBlocks.poke();
		return bn.bp;
	}

	@Override
	public void moveNext()
	{
		searchForTreeBlocks(_treeBlocks.shift());
		if (_treeBlocks.size() == 0)
		{
			_isDone = true;
		}
	}

	private void searchForTreeBlocks(BlockNode bn)
	{
		BlockPosition bp = bn.bp;
		Map<Integer, IFactoryHarvestable> harvestables = MFRRegistry.getHarvestables();
		BlockNode cur;
		if (_harvestMode.isInverted)
			cur = BlockPool.getNext(bp.x, bp.y - 1, bp.z);
		else
			cur = BlockPool.getNext(bp.x, bp.y + 1, bp.z);
		if (isValid(cur.bp, harvestables))
			_treeBlocks.push(cur);
		else
			cur.free();

		for (ForgeDirection side : SIDES)
		{
			cur = BlockPool.getNext(bp.x + side.offsetX, bp.y + side.offsetY, bp.z + side.offsetZ);
			if (isValid(cur.bp, harvestables))
				_treeBlocks.push(cur);
			else
				cur.free();
		}

		if (_harvestMode.isInverted)
			cur = BlockPool.getNext(bp.x, bp.y + 1, bp.z);
		else
			cur = BlockPool.getNext(bp.x, bp.y - 1, bp.z);
		if (isValid(cur.bp, harvestables))
			_treeBlocks.push(cur);
		else
			cur.free();
		bn.free();
	}

	private boolean isValid(BlockPosition bp, Map<Integer, IFactoryHarvestable> harvestables)
	{
		if ((bp.x > _treeArea.xMax) | (bp.x < _treeArea.xMin) |
				(bp.z > _treeArea.zMax) | (bp.z < _treeArea.zMin) |
				(bp.y > _treeArea.yMax) | (bp.y < _treeArea.yMin) ||
				!_world.blockExists(bp.x, bp.y, bp.z))
			return false;

		Integer blockId = _world.getBlockId(bp.x, bp.y, bp.z);
		if (harvestables.containsKey(blockId))
		{
			HarvestType obj = harvestables.get(blockId).getHarvestType();
			return obj == HarvestType.TreeFlipped |
					obj == HarvestType.TreeLeaf |
					obj == HarvestType.Tree;
		}
		return false;
	}

	@Override
	public void reset(World world, Area treeArea, HarvestMode harvestMode)
	{
		_world = world;
		_harvestMode = harvestMode;
		_treeArea = treeArea;
		_isDone = false;
		_treeBlocks = new BlockPool();
		BlockPosition bp = treeArea.getOrigin();
		_treeBlocks.push(BlockPool.getNext(bp.x, bp.y, bp.z));
	}

	@Override
	public boolean getIsDone()
	{
		return _isDone;
	}

	@Override
	public BlockPosition getOrigin()
	{
		return _treeArea.getOrigin();
	}
}
