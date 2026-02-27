package Utils;

import java.util.Random;

/**
 * Generates anonymous blind usernames like "SilentFalcon4821".
 * Runs entirely offline — no API call needed.
 * Format: Adjective + Animal/Noun + 4-digit number
 */
public class UsernameGenerator {

    private static final String[] ADJECTIVES = {
            "Silent", "Swift", "Brave", "Clever", "Calm",
            "Bold", "Bright", "Dark", "Iron", "Jade",
            "Lone", "Noble", "Rapid", "Sharp", "Solid",
            "Stern", "Stone", "Storm", "Wild", "Wise",
            "Amber", "Azure", "Blaze", "Cedar", "Coral",
            "Crisp", "Crown", "Dusk", "Echo", "Ember",
            "Frost", "Ghost", "Gloom", "Gold", "Grand",
            "Grim", "Haze", "Indigo", "Ivory", "Lunar",
            "Marble", "Mystic", "Onyx", "Prism", "Raven",
            "Ruby", "Rustic", "Sage", "Scarlet", "Silver",
            "Slate", "Solar", "Sonic", "Steel", "Titan"
    };

    private static final String[] NOUNS = {
            "Falcon", "Wolf", "Eagle", "Bear", "Fox",
            "Hawk", "Lion", "Lynx", "Owl", "Puma",
            "Raven", "Tiger", "Viper", "Cobra", "Crane",
            "Deer", "Drake", "Finch", "Heron", "Horse",
            "Jaguar", "Kite", "Leopard", "Marten", "Merlin",
            "Mink", "Moose", "Osprey", "Otter", "Panther",
            "Pelican", "Phoenix", "Pike", "Puffin", "Quail",
            "Robin", "Sable", "Salmon", "Seal", "Shark",
            "Sparrow", "Stallion", "Swan", "Teal", "Thrush",
            "Toucan", "Trout", "Turtle", "Walrus", "Wren"
    };

    private static final Random RNG = new Random();

    /**
     * Generates a unique-ish blind username.
     * Example outputs: "SilentFalcon4821", "IronWolf2047", "AzureOwl9314"
     */
    public static String generate() {
        String adj  = ADJECTIVES[RNG.nextInt(ADJECTIVES.length)];
        String noun = NOUNS[RNG.nextInt(NOUNS.length)];
        int    num  = 1000 + RNG.nextInt(9000); // 4-digit number
        return adj + noun + num;
    }
}