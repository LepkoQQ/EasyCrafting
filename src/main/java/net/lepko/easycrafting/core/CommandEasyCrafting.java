package net.lepko.easycrafting.core;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.config.ConfigHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class CommandEasyCrafting extends CommandBase {

    @Override
    public int compareTo(Object o) {
        return getCommandName().compareTo(((ICommand) o).getCommandName());
    }

    @Override
    public String getCommandName() {
        return "easycrafting";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return EnumChatFormatting.AQUA + "/" + getCommandName() + " [version | recursion]";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
        return true;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "version", "recursion");
        }
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        if (args[0].equalsIgnoreCase("version")) {
            processVersionCommand(sender);
        } else if (args[0].equalsIgnoreCase("recursion")) {
            processRecursionCommand(sender, args);
        } else {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private void processRecursionCommand(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> Recursion is: " + ConfigHandler.MAX_RECURSION));
        } else if (args.length == 2) {
            int number = parseIntBounded(sender, args[1], 0, 10);
            ConfigHandler.setRecursion(number);
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> Recursion set: " + number));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Usage:"));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "  /easycrafting recursion " + EnumChatFormatting.RED + " - Get current recursion value"));
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.AQUA + "  /easycrafting recursion <value>" + EnumChatFormatting.RED + " - Set new recursion value"));
        }
    }

    private void processVersionCommand(ICommandSender sender) {
        sender.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "> " + Ref.MOD_NAME + " version " + Ref.VERSION));
    }
}
