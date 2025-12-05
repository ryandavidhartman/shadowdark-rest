// Seed the Backgrounds collection from the BFRPG table.
// Run with: mongosh --file data/seed-backgrounds.js "mongodb://localhost:27017/shadowdark"

/* global Mongo, ObjectId */

const positionalArgs = process.argv.slice(2).filter(a => !a.startsWith('--'));
const uriArg = positionalArgs.find(a => a.startsWith('mongodb'));
const dbArg = positionalArgs.find(a => a.startsWith('db='));
const collectionArg = positionalArgs.find(a => a.startsWith('collection='));

const uri = uriArg || process.env.MONGO_URI || 'mongodb://localhost:27017/shadowdark';

const parsed = new URL(uri);
const dbName =
  (dbArg && dbArg.split('=')[1]) ||
  process.env.MONGO_DB ||
  (parsed.pathname && parsed.pathname !== '/' ? parsed.pathname.slice(1) : 'shadowdark');
const collectionName =
  (collectionArg && collectionArg.split('=')[1]) ||
  process.env.MONGO_COLLECTION ||
  'Backgrounds';

const backgrounds = JSON.parse(`[
  {
    "name": "Alchemist",
    "range": {
      "min": 1,
      "max": 7
    },
    "possessions": "Staff; Oil, 1 flask",
    "details": "You brewed volatile draughts for coin and questions you'd rather not answer."
  },
  {
    "name": "Animal trainer",
    "range": {
      "min": 8,
      "max": 14
    },
    "possessions": "Club; Pony",
    "details": "You read beasts like books and calm them with a word and a palm."
  },
  {
    "name": "Apothecarist",
    "range": {
      "min": 15,
      "max": 21
    },
    "possessions": "Cudgel (as staff); Steel vial",
    "details": "You traded in remedies, rumors, and the bitter taste of truth."
  },
  {
    "name": "Armorer",
    "range": {
      "min": 22,
      "max": 28
    },
    "possessions": "Hammer (as club); Iron helmet",
    "details": "You hammered plate and patch; few know steel's moods like you."
  },
  {
    "name": "Artisan",
    "range": {
      "min": 29,
      "max": 35
    },
    "possessions": "Staff; Clay, 1 lb.",
    "details": "Craft scarred your hands and honed your will."
  },
  {
    "name": "Astrologer",
    "range": {
      "min": 36,
      "max": 40
    },
    "possessions": "Dagger; Spyglass",
    "details": "You sell hope and doom to those who can't tell the difference."
  },
  {
    "name": "Attendant",
    "range": {
      "min": 41,
      "max": 70
    },
    "possessions": "Dagger; Silver comb",
    "details": "You served your betters in silence and learned more than they guessed."
  },
  {
    "name": "Banished",
    "range": {
      "min": 71,
      "max": 77
    },
    "possessions": "Club; Small toolkit",
    "details": "Your people cast you out for supposed crimes."
  },
  {
    "name": "Barbarian",
    "range": {
      "min": 78,
      "max": 84
    },
    "possessions": "Club; Fetish necklace",
    "details": "You left the horde, but it never quite left you."
  },
  {
    "name": "Barber",
    "range": {
      "min": 85,
      "max": 91
    },
    "possessions": "Razor (as dagger); Scissors",
    "details": "Steel, steadiness, and secrets-your daily tools."
  },
  {
    "name": "Barkeep",
    "range": {
      "min": 92,
      "max": 98
    },
    "possessions": "Club; Tankard",
    "details": "Heard more secrets over ale than most priests in confession."
  },
  {
    "name": "Barrister",
    "range": {
      "min": 99,
      "max": 105
    },
    "possessions": "Quill (as dart); Book",
    "details": "You twist words until truth gives up."
  },
  {
    "name": "Beadle",
    "range": {
      "min": 106,
      "max": 117
    },
    "possessions": "Staff; Holy symbol",
    "details": "You keep order with ceremony and a firm hand."
  },
  {
    "name": "Beekeeper",
    "range": {
      "min": 118,
      "max": 124
    },
    "possessions": "Staff; Jar of honey",
    "details": "You learned sweet patience among stings."
  },
  {
    "name": "Beggar",
    "range": {
      "min": 125,
      "max": 131
    },
    "possessions": "Club; Wooden bowl",
    "details": "You've learned the art of survival one copper at a time."
  },
  {
    "name": "Blacksmith",
    "range": {
      "min": 132,
      "max": 143
    },
    "possessions": "Hammer (as club); Steel tongs",
    "details": "You forged strength from fire and long nights."
  },
  {
    "name": "Bonepicker",
    "range": {
      "min": 144,
      "max": 150
    },
    "possessions": "Club; Pouch of trinkets",
    "details": "You sift through ruins and corpses for stories and silver."
  },
  {
    "name": "Bookbinder",
    "range": {
      "min": 151,
      "max": 157
    },
    "possessions": "Awl (as dagger); Glue pot",
    "details": "You learned that even lies can be bound in leather."
  },
  {
    "name": "Butcher",
    "range": {
      "min": 158,
      "max": 164
    },
    "possessions": "Cleaver (as axe); Side of beef",
    "details": "You've seen life's end one cut at a time."
  },
  {
    "name": "Caravan guard",
    "range": {
      "min": 165,
      "max": 176
    },
    "possessions": "Short sword; Linen, 1 yard",
    "details": "You've stood watch in the rain and seen too much blood."
  },
  {
    "name": "Cartographer",
    "range": {
      "min": 177,
      "max": 183
    },
    "possessions": "Dagger; Map case and quill",
    "details": "You draw the world to remember where you've already bled."
  },
  {
    "name": "Chandler",
    "range": {
      "min": 184,
      "max": 190
    },
    "possessions": "Scissors (as dagger); Candles, 20",
    "details": "You poured light for those who feared the dark."
  },
  {
    "name": "Charcoal-Burner",
    "range": {
      "min": 191,
      "max": 197
    },
    "possessions": "Staff; Sooted hood",
    "details": "You tend smoky pits where wood becomes fuel and lungs grow old."
  },
  {
    "name": "Chest-maker",
    "range": {
      "min": 198,
      "max": 204
    },
    "possessions": "Chisel (as dagger); Wood, 10 lbs.",
    "details": "You built what others hide their treasures in."
  },
  {
    "name": "Chirurgeon",
    "range": {
      "min": 205,
      "max": 211
    },
    "possessions": "Club; Small toolkit",
    "details": "You know anatomy, surgery, and first aid."
  },
  {
    "name": "Cobbler",
    "range": {
      "min": 212,
      "max": 219
    },
    "possessions": "Awl (as dagger); Shoehorn",
    "details": "You mend what others wear out-boots, tempers, and patience."
  },
  {
    "name": "Confidence artist",
    "range": {
      "min": 220,
      "max": 227
    },
    "possessions": "Dagger; Quality cloak",
    "details": "You lie smoother than silk and quicker than regret."
  },
  {
    "name": "Cooper",
    "range": {
      "min": 228,
      "max": 235
    },
    "possessions": "Crowbar (as club); Barrel",
    "details": "You know how to bend wood and make it hold."
  },
  {
    "name": "Costermonger",
    "range": {
      "min": 236,
      "max": 243
    },
    "possessions": "Knife (as dagger); Fruit",
    "details": "You've peddled wares and rumors in equal measure."
  },
  {
    "name": "Cult Initiate",
    "range": {
      "min": 244,
      "max": 248
    },
    "possessions": "Club; Small toolkit",
    "details": "You know blasphemous secrets and rituals."
  },
  {
    "name": "Cutpurse",
    "range": {
      "min": 249,
      "max": 260
    },
    "possessions": "Dagger; Small chest",
    "details": "You live by your wits and the weight of other people's purses."
  },
  {
    "name": "Ditch Digger",
    "range": {
      "min": 261,
      "max": 290
    },
    "possessions": "Shovel (as staff); Muddy boots",
    "details": "Earned strength and silence under a hard sun."
  },
  {
    "name": "Dancer",
    "range": {
      "min": 291,
      "max": 298
    },
    "possessions": "Dagger; Bells and ribbons",
    "details": "You learned grace before you learned caution."
  },
  {
    "name": "Dock worker",
    "range": {
      "min": 299,
      "max": 328
    },
    "possessions": "Pole (as staff); Coil of rope",
    "details": "You've hauled cargo and corpses alike."
  },
  {
    "name": "Dyer",
    "range": {
      "min": 329,
      "max": 336
    },
    "possessions": "Staff; Fabric, 3 yards",
    "details": "You know which dyes fade fast and which kill slow."
  },
  {
    "name": "Falconer",
    "range": {
      "min": 337,
      "max": 344
    },
    "possessions": "Dagger; Falcon",
    "details": "You teach wild tempers obedience-and envy their freedom."
  },
  {
    "name": "Farmer*",
    "range": {
      "min": 345,
      "max": 374
    },
    "possessions": "Pitchfork (as spear); Animal**",
    "details": "You know the patience of the plow and the moods of the soil."
  },
  {
    "name": "Forester",
    "range": {
      "min": 375,
      "max": 382
    },
    "possessions": "Staff; Herbs, 1 lb.",
    "details": "You read the grain of wood and the weather of the wilds."
  },
  {
    "name": "Fortune-teller",
    "range": {
      "min": 383,
      "max": 387
    },
    "possessions": "Dagger; Tarot deck",
    "details": "You sell hope to fools and hear too much truth."
  },
  {
    "name": "Gambler",
    "range": {
      "min": 388,
      "max": 395
    },
    "possessions": "Club; Dice",
    "details": "You live by chance, lie by instinct, and die by odds."
  },
  {
    "name": "Gravedigger's Apprentice",
    "range": {
      "min": 396,
      "max": 403
    },
    "possessions": "Shovel (as staff); Lantern",
    "details": "You dug beside the dead until they stopped frightening you."
  },
  {
    "name": "Glassblower",
    "range": {
      "min": 404,
      "max": 411
    },
    "possessions": "Hammer (as club); Glass beads",
    "details": "You shaped beauty that shatters at a breath."
  },
  {
    "name": "Glove maker",
    "range": {
      "min": 412,
      "max": 419
    },
    "possessions": "Awl (as dagger); Gloves, 4 pairs",
    "details": "You stitched fine seams while plotting your escape."
  },
  {
    "name": "Grave digger",
    "range": {
      "min": 420,
      "max": 427
    },
    "possessions": "Shovel (as staff); Trowel",
    "details": "You handle death calmly-it's the living that cause trouble."
  },
  {
    "name": "Guild beggar",
    "range": {
      "min": 428,
      "max": 457
    },
    "possessions": "Sling; Crutches",
    "details": "You learned the city's back ways and how to sleep with one eye open."
  },
  {
    "name": "Hearth Keeper",
    "range": {
      "min": 458,
      "max": 465
    },
    "possessions": "Club; Iron poker",
    "details": "You kept the fires burning while others fought the cold and the dark."
  },
  {
    "name": "Haberdasher",
    "range": {
      "min": 466,
      "max": 473
    },
    "possessions": "Scissors (as dagger); Fine suits, 3 sets",
    "details": "From long years as a haberdasher, you've learned how to endure and adapt."
  },
  {
    "name": "Healer",
    "range": {
      "min": 474,
      "max": 503
    },
    "possessions": "Club; Holy water, 1 vial",
    "details": "You've bound wounds and brewed cures from roots and chance."
  },
  {
    "name": "Herbalist",
    "range": {
      "min": 504,
      "max": 511
    },
    "possessions": "Club; Herbs, 1 lb.",
    "details": "You know plants, medicines, and poisons."
  },
  {
    "name": "Herder",
    "range": {
      "min": 512,
      "max": 519
    },
    "possessions": "Staff; Herding dog**",
    "details": "You wander where grass grows, counting more beasts than friends."
  },
  {
    "name": "Herder",
    "range": {
      "min": 520,
      "max": 527
    },
    "possessions": "Staff; Sow**",
    "details": "You wander where grass grows, counting more beasts than friends."
  },
  {
    "name": "Hunter",
    "range": {
      "min": 528,
      "max": 557
    },
    "possessions": "Shortbow; Deer pelt",
    "details": "You know how to wait, and when not to miss."
  },
  {
    "name": "Indentured servant",
    "range": {
      "min": 558,
      "max": 565
    },
    "possessions": "Staff; Locket",
    "details": "You bought your freedom one scar at a time."
  },
  {
    "name": "Jester",
    "range": {
      "min": 566,
      "max": 573
    },
    "possessions": "Dart; Silk clothes",
    "details": "You mask fear with laughter and hide truth in jokes."
  },
  {
    "name": "Jeweler",
    "range": {
      "min": 574,
      "max": 581
    },
    "possessions": "Dagger; Gem worth 20 gp",
    "details": "You can easily appraise value and authenticity."
  },
  {
    "name": "Jailer",
    "range": {
      "min": 582,
      "max": 589
    },
    "possessions": "Club; Set of keys",
    "details": "You've watched men rot and wondered which deserved it."
  },
  {
    "name": "Locksmith",
    "range": {
      "min": 590,
      "max": 597
    },
    "possessions": "Dagger; Lockpicks",
    "details": "Knows every secret a keyhole can tell."
  },
  {
    "name": "Mariner",
    "range": {
      "min": 598,
      "max": 605
    },
    "possessions": "Knife (as dagger); Sailcloth, 2 yards",
    "details": "You belong to the tide more than any port."
  },
  {
    "name": "Mendicant",
    "range": {
      "min": 606,
      "max": 613
    },
    "possessions": "Club; Begging Bowl",
    "details": "You beg from saints, sinners, and anything that listens."
  },
  {
    "name": "Mercenary",
    "range": {
      "min": 614,
      "max": 621
    },
    "possessions": "Longsword; Hide armor",
    "details": "You fought friend and foe alike for your coin."
  },
  {
    "name": "Merchant",
    "range": {
      "min": 622,
      "max": 651
    },
    "possessions": "Dagger; 4 gp, 14 sp, 27 cp",
    "details": "You've learned the worth of words and the price of everything else."
  },
  {
    "name": "Miller/baker",
    "range": {
      "min": 652,
      "max": 659
    },
    "possessions": "Club; Flour, 1 lb.",
    "details": "You measure life in turns of the wheel and handfuls of grain."
  },
  {
    "name": "Miner",
    "range": {
      "min": 660,
      "max": 667
    },
    "possessions": "Pick (as club); Lantern",
    "details": "You've dug deep where light fears to go."
  },
  {
    "name": "Minstrel",
    "range": {
      "min": 668,
      "max": 675
    },
    "possessions": "Dagger; Instrument (simple)",
    "details": "You've traveled far with your charm and talent."
  },
  {
    "name": "Mortician",
    "range": {
      "min": 676,
      "max": 683
    },
    "possessions": "Razor (as dagger); Embalming salts",
    "details": "You dress the dead better than the living."
  },
  {
    "name": "Moneylender",
    "range": {
      "min": 684,
      "max": 691
    },
    "possessions": "Short sword; 5 gp, 10 sp, 200 cp",
    "details": "You trade in trust, and collect what's left of it."
  },
  {
    "name": "Mushroom-farmer",
    "range": {
      "min": 692,
      "max": 699
    },
    "possessions": "Shovel (as staff); Sack",
    "details": "You know the patience of the plow and the moods of the soil."
  },
  {
    "name": "Navigator",
    "range": {
      "min": 700,
      "max": 707
    },
    "possessions": "Shortbow; Spyglass",
    "details": "The road of a navigator is rough, but you know every turn of it."
  },
  {
    "name": "Noble",
    "range": {
      "min": 708,
      "max": 712
    },
    "possessions": "Longsword; Gold ring worth 10 gp",
    "details": "A famous name has opened many doors for you."
  },
  {
    "name": "Noble (Disgraced)",
    "range": {
      "min": 713,
      "max": 717
    },
    "possessions": "Dagger; Tattered seal",
    "details": "You learned grace, deceit, and how to bow without kneeling."
  },
  {
    "name": "Orphan",
    "range": {
      "min": 718,
      "max": 725
    },
    "possessions": "Club; Rag doll",
    "details": "An unusual guardian rescued and raised you."
  },
  {
    "name": "Ostler",
    "range": {
      "min": 726,
      "max": 733
    },
    "possessions": "Staff; Bridle",
    "details": "You know beasts better than men."
  },
  {
    "name": "Outlaw",
    "range": {
      "min": 734,
      "max": 741
    },
    "possessions": "Short sword; Leather armor",
    "details": "Faces blur on the road, but yours is remembered by the wrong people."
  },
  {
    "name": "Painter",
    "range": {
      "min": 742,
      "max": 749
    },
    "possessions": "Dagger; Pigment box",
    "details": "Sees beauty and doom in every shade of color."
  },
  {
    "name": "Ranger",
    "range": {
      "min": 750,
      "max": 757
    },
    "possessions": "Shortbow; Snare and 10' cord",
    "details": "The woods and wilds are your true home."
  },
  {
    "name": "Rat-catcher",
    "range": {
      "min": 758,
      "max": 765
    },
    "possessions": "Club; Net",
    "details": "You learned the tunnels by smell and the squeal of dying things."
  },
  {
    "name": "Relic Hunter",
    "range": {
      "min": 766,
      "max": 770
    },
    "possessions": "Short sword; Tarnished holy symbol",
    "details": "You chase legends for gold, glory, or ghosts that won't rest."
  },
  {
    "name": "Rope maker",
    "range": {
      "min": 771,
      "max": 778
    },
    "possessions": "Knife (as dagger); Rope, 100'",
    "details": "You know every knot that binds, breaks, or saves a life."
  },
  {
    "name": "Runesmith",
    "range": {
      "min": 779,
      "max": 786
    },
    "possessions": "Hammer (as club); Engraver's chisel",
    "details": "You carve words of power that remember more than you do."
  },
  {
    "name": "Sage",
    "range": {
      "min": 787,
      "max": 794
    },
    "possessions": "Dagger; Parchment and quill pen",
    "details": "Your past as a sage still echoes in every choice you make."
  },
  {
    "name": "Sailor",
    "range": {
      "min": 795,
      "max": 806
    },
    "possessions": "Club; Coil of rope (25')",
    "details": "Pirate, privateer, or merchant, the seas are yours."
  },
  {
    "name": "Scholar",
    "range": {
      "min": 807,
      "max": 818
    },
    "possessions": "Dagger; Ink and quill",
    "details": "You know much about ancient history and lore."
  },
  {
    "name": "Scout",
    "range": {
      "min": 819,
      "max": 826
    },
    "possessions": "Shortbow; Snare and 10' cord",
    "details": "You survived on stealth, observation, and speed."
  },
  {
    "name": "Scribe",
    "range": {
      "min": 827,
      "max": 834
    },
    "possessions": "Dart; Parchment, 10 sheets",
    "details": "You've copied more lies and legends than you can count."
  },
  {
    "name": "Scribe (Itinerant)",
    "range": {
      "min": 835,
      "max": 842
    },
    "possessions": "Dagger; Journal and quill",
    "details": "Wanders with words, binding strangers' tales into their own."
  },
  {
    "name": "Shaman",
    "range": {
      "min": 843,
      "max": 847
    },
    "possessions": "Mace; Bone fetish",
    "details": "Spirits whisper truths no one else dares hear."
  },
  {
    "name": "Slave",
    "range": {
      "min": 848,
      "max": 855
    },
    "possessions": "Club; Strange-looking rock",
    "details": "You've learned obedience, and the price of freedom."
  },
  {
    "name": "Smuggler",
    "range": {
      "min": 856,
      "max": 863
    },
    "possessions": "Sling; Waterproof sack",
    "details": "You know which tides hide the truth and which wash it clean."
  },
  {
    "name": "Soldier",
    "range": {
      "min": 864,
      "max": 893
    },
    "possessions": "Spear; Shield",
    "details": "You served as a fighter in an organized way."
  },
  {
    "name": "Spy",
    "range": {
      "min": 894,
      "max": 898
    },
    "possessions": "Dagger; Disguise Kit",
    "details": "Walks among enemies with a dozen borrowed faces."
  },
  {
    "name": "Squire",
    "range": {
      "min": 899,
      "max": 906
    },
    "possessions": "Longsword; Steel helmet",
    "details": "You once dreamed of knighthood; now you dream of survival."
  },
  {
    "name": "Stonemason",
    "range": {
      "min": 907,
      "max": 914
    },
    "possessions": "Hammer; Fine stone, 10 lbs.",
    "details": "You built walls meant to outlast men."
  },
  {
    "name": "Tax collector",
    "range": {
      "min": 915,
      "max": 922
    },
    "possessions": "Longsword; Ledger",
    "details": "You measure lives in coin and resentment."
  },
  {
    "name": "Thieves' Guild",
    "range": {
      "min": 923,
      "max": 930
    },
    "possessions": "Club; Small toolkit",
    "details": "You have connections, contacts, and debts."
  },
  {
    "name": "Torchbearer",
    "range": {
      "min": 931,
      "max": 938
    },
    "possessions": "Club; Lantern and 2 flasks of oil",
    "details": "You walk behind heroes, lighting their triumphs and graves alike."
  },
  {
    "name": "Trader",
    "range": {
      "min": 939,
      "max": 946
    },
    "possessions": "Short sword; 20 sp",
    "details": "You earned your keep on the road and know a fair deal when you see one."
  },
  {
    "name": "Trapper",
    "range": {
      "min": 947,
      "max": 954
    },
    "possessions": "Sling; Badger pelt",
    "details": "You know how to wait, and when not to miss."
  },
  {
    "name": "Urchin",
    "range": {
      "min": 955,
      "max": 962
    },
    "possessions": "Stick (as club); Begging bowl",
    "details": "You grew up on the merciless streets of a large city."
  },
  {
    "name": "Vagrant",
    "range": {
      "min": 963,
      "max": 992
    },
    "possessions": "Club; Begging bowl",
    "details": "The road is your bed, the stars your ceiling, and hunger your companion."
  },
  {
    "name": "Wainwright",
    "range": {
      "min": 993,
      "max": 1000
    },
    "possessions": "Club; Pushcart***",
    "details": "You built what carries burdens when legs give out."
  }
]`);

const conn = new Mongo(uri);
const db = conn.getDB(dbName);
const collection = db.getCollection(collectionName);

print(`Seeding ${collectionName} in ${dbName} at ${uri}`);
collection.deleteMany({});
collection.insertMany(
  backgrounds.map(bg => ({
    _id: new ObjectId(),
    name: bg.name,
    range: bg.range,
    possessions: bg.possessions,
    details: bg.details,
  })),
);

print('Done.');
