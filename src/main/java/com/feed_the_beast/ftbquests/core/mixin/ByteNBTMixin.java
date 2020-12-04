package com.feed_the_beast.ftbquests.core.mixin;

import com.feed_the_beast.ftbquests.core.ByteNBTFTBQ;
import net.minecraft.nbt.ByteNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author LatvianModder
 */
@Mixin(ByteNBT.class)
public abstract class ByteNBTMixin implements ByteNBTFTBQ
{
	private boolean isBooleanFTBQ;

	@Inject(method = "toString", at = @At("HEAD"), cancellable = true)
	public void toStringFTBQ(CallbackInfoReturnable<String> ci)
	{
		if (isBooleanFTBQ)
		{
			ci.setReturnValue(((ByteNBT) (Object) this).getByte() == 1 ? "true" : "false");
		}
	}

	@Override
	public void setBooleanFTBQ()
	{
		isBooleanFTBQ = true;
	}
}
