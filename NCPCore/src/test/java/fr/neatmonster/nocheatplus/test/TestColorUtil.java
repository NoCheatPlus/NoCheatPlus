package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.junit.Test;

import fr.neatmonster.nocheatplus.utilities.ColorUtil;

public class TestColorUtil {
	@Test
	public void testRemoveColor(){
		List<String[]> items = new LinkedList<String[]>();
		for (String[] item : new String[][]{
				{"", ""},
				{" ", " "},
				{"&", "&"},
				{"&&", "&&"},
				{"o3rg7cbo'!§)=?%V823rg7c", "o3rg7cbo'!§)=?%V823rg7c"},
		}){
			items.add(item);
		}
		String[][] generic = new String[][]{
				{"&/&/&/", ""},
				{"&/&/", ""},
				{" &/&/ ", "  "},
				{" &/&/", " "},
				{"&/", ""},
				{"&/ ", " "},
				{" &/", " "},
				{"123&/123", "123123"},
				
		};
		for (ChatColor color : ChatColor.values()){
			char c = color.getChar();
			for (String[] pattern : generic){
				items.add(new String[]{ pattern[0].replace('/', c), pattern[1]});
			}
		}
		int i = 0;
		for (String[] item : items){
			String input = item[0];
			String expectedOutput = item[1];
			String detail = "no details.";
			String output = "(ERROR)"; 
			try{
				output = ColorUtil.removeColors(input);
			}
			catch(Throwable t){
				detail = t.getClass().getSimpleName() + ": " + t.getMessage();
			}
			if (!expectedOutput.equals(output)){
				fail("Wrong output at index " + i + " for input '" + input + "', expected '" + expectedOutput + "', but got instead: '" + output + "', details: " + detail );
			}
			i ++;
		}
	}
}
