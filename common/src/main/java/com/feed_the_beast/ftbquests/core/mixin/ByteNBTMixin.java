package com.feed_the_beast.ftbquests.core.mixin;

import com.feed_the_beast.ftbquests.core.ByteNBTFTBQ;
import net.minecraft.nbt.ByteTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author LatvianModder
 */
@Mixin(ByteTag.class)
public abstract class ByteNBTMixin implements ByteNBTFTBQ
{
	@Unique
	private boolean isBooleanFTBQ;

	@Inject(method = "toString", at = @At("HEAD"), cancellable = true)
	public void toStringFTBQ(CallbackInfoReturnable<String> ci)
	{
		if (isBooleanFTBQ)
		{
			ci.setReturnValue(((ByteTag) (Object) this).getAsByte() == 1 ? "true" : "false");
		}
	}

	@Override
	public void setBooleanFTBQ()
	{
		isBooleanFTBQ = true;
	}
}
