// Seed the Spells collection with core spell data.
// Run with: mongosh --file data/seed-spells.js "mongodb://localhost:27017/shadowdark"

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
  'Spells';

const spells = [
  {
    "name": "Acid Arrow",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Focus",
    "dc": 12,
    "description": "You conjure a corrosive bolt that hits one foe, dealing 1d6 acid damage a round. The bolt remains in the target for as long as you focus.\n\nImage: Acid Arrow",
    "damage": "1d6",
    "damageType": "acid"
  },
  {
    "name": "Alarm",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "1 day",
    "dc": 11,
    "description": "You touch one object, such as a door threshold, setting a magical alarm on it. If any creature you do not designate while casting the spell touches or crosses past the object, a magical bell sounds in your head."
  },
  {
    "name": "Alter Self",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You magically change your physical form, gaining one feature that modifies your existing anatomy."
  },
  {
    "name": "Anathema",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Instant",
    "dc": 15,
    "description": "All allies revile and abandon the creature you touch for 1 day. Each time you or your allies harm the target, its former allies may pass a DC 15 Wisdom check to end the effects of the spell."
  },
  {
    "name": "Ancestral Guidance",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "Your voice carries the insight of ancient mystics, granting your allies in near distance advantage on all spellcasting, herbal, and elixir checks."
  },
  {
    "name": "Animal Totem",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1 Day",
    "dc": 13,
    "description": "A willing animal you touch is transformed into a small wooden statue in your inventory.\nYou can return it to natural form at any time before the duration expires."
  },
  {
    "name": "Animate Dead",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "1 day",
    "dc": 13,
    "description": "You touch one humanoid's remains, and it rises as a zombie or skeleton under your control. The remains must have at least three limbs and its head intact. The undead creature acts on your turn. After 1 day, the creature collapses into grave dust."
  },
  {
    "name": "Antimagic Shell",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "Focus",
    "dc": 15,
    "description": "An invisible, near-sized cube of null-magic appears centered on you. Within the cube, no spells can be cast. Magic items and spells have no effect in the zone, and no magic can enter. The cube moves with you. Spells such as dispel magic have no effect on it. Another antimagic shell does not affect this one."
  },
  {
    "name": "Arbor Vantage",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "Any ally within near of a tree you enchant makes attacks with ADV."
  },
  {
    "name": "Arcane Eye",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You conjure an invisible, grape-sized eye within range. You can see through the eye. It can see in the dark out to near range, fly near on your turn, and squeeze through openings as narrow as a keyhole."
  },
  {
    "name": "Armaments of the Ancients",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "You invoke ancestral grace, imbuing your near allies' weapons with magic. They all become +1 magical weapons."
  },
  {
    "name": "Arrow of Hornets",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "A mass of hornets you summon coalesce into an arrow that hits a target, dealing 1d6 piercing damage.",
    "damage": "1d6",
    "damageType": "piercing"
  },
  {
    "name": "Augury",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Instant",
    "dc": 12,
    "description": "You interpret the meaning of supernatural portents and omens. Ask the GM one question about a specific course of action. The GM says whether the action will lead to \"weal\" or \"woe.\""
  },
  {
    "name": "Barkhide",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 11,
    "description": "An animal or giant animal's skin you touch hardens like tree bark. For the spell's duration, the animal's armor class is increased by 2. Can also be cast on Beastmasters."
  },
  {
    "name": "Beguile",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You conjure a convincing visible and audible illusion within range. Creatures who perceive the illusion react to it as though it were real, although it can't cause actual harm. Touching the illusion instantly reveals its false nature. You may force a creature who interacts with the illusion to make a DC 15 Wisdom check. If the creature fails, it is enchanted by the illusion for the spell's duration and seeks to protect it.",
    "opposed": 1,
    "opposedDc": 15,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Bite of the Mantis",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You invoke the fierce spirit of the mantis. Enemies a near distance from you who damage you or your allies inflict an additional half damage to themselves.\nTargets know this damage is coming from you."
  },
  {
    "name": "Black Blood",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 14,
    "description": "With the touch of your hand, any living creature whose LV is half of yours or less must make a CON check vs. your spellcasting check or be turned into a zombie under your control.\nThe zombie retains the original shape, LV and HP, but loses its abilities, using zombie stats and abilities.\nTotal undead LV under your control cannot exceed your level. (See Raising the Dead for more information.)",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Bless",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 12,
    "description": "One creature you touch gains a luck token."
  },
  {
    "name": "Blind",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You utter a divine censure, blinding one creature you can see in range. The creature has disadvantage on tasks requiring sight."
  },
  {
    "name": "Blood Pact",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 13,
    "description": "You form a life-preserving contract among willing allies in near range.\nIf any ally is reduced to 0 HP, that ally automatically drains 1 HP from all other allies who have at least 2 HP in near range.\nBlood Pact can only be cast once per day."
  },
  {
    "name": "Bloomquake",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 13,
    "description": "You cause thousands of flowers to burst from the ground, creating an earthquake centered on a target within range.\nAll creatures in a near area from target location must make a DEX check vs. your spellcasting check or fall and take 2d6 blunt damage.",
    "damage": "2d6",
    "damageType": "blunt",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "dex"
  },
  {
    "name": "Bogboil",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Far",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You turn a near-sized cube of ground within range into a muddy, boiling bog of quicksand. A creature stuck in the bog can't move and must succeed on a Dexterity check vs. your spellcasting check to free itself.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "dexterity"
  },
  {
    "name": "Bone Ballista",
    "tier": 4,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Close",
    "duration": "Focus",
    "dc": 14,
    "description": "You form a ballista made of bone from a corpse you touch.\nIt fires a bolt of bone shards during your turn, automatically hitting a target in near range, doing 4d6 piercing damage.\nThe bone ballista has 10 HP, 11 AC, and can be damaged by physical or magical attacks.",
    "damage": "4d6",
    "damageType": "piercing"
  },
  {
    "name": "Bone Storm",
    "tier": 5,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "You call upon deathly winds to create a tornado of whirling bones that cover a near cube.\nAny creatures caught inside take 5d6 blunt damage per round.\nYou can forgo moving to move the storm a near distance.\n\nImage: Bone Storm",
    "damage": "5d6",
    "damageType": "blunt"
  },
  {
    "name": "Bonebound Sentry",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 12,
    "description": "Using spectral energy, you double the size of one undead minion that you control and root it in place.\nThe bonebound minion cannot move, but its maximum HP is doubled and it deals an extra die of damage.\nYou may dispel Bonebound Sentry at any time."
  },
  {
    "name": "Bonefire",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "1 rest",
    "dc": 12,
    "description": "You light a corpse into a magical campfire that cannot be moved, illuminating a near distance.\nThere is a 50% chance that the necromantic energy from the fire will deter random monsters.\n\nImage: Bonefire"
  },
  {
    "name": "Boneskin",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 12,
    "description": "Your skin becomes covered in hardened bone armor. For the spell's duration, your armor class becomes 17 (20 on a critical spellcasting check.) [Adjust the effect as needed.]"
  },
  {
    "name": "Bramble Bridge",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "Your song stirs the dormant energy within the surrounding plants, prompting them to sprout and weave together into a bridge, stairway, or ramp, spanning a near-sized cube.\nAttempts to maintain this chant are made with advantage."
  },
  {
    "name": "Broomstick",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 13,
    "description": "You conjure a flying broomstick in your hand. The broomstick's rider can fly a near distance each round and can hover in place."
  },
  {
    "name": "Burning Hands",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "You spread your fingers with thumbs touching, unleashing a circle of flame that roars out to a close area around where you stand. Creatures within the area of effect take 1d6 fire damage, and flammable objects catch fire.",
    "damage": "1d6",
    "damageType": "fire"
  },
  {
    "name": "Cacklerot",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Focus",
    "dc": 12,
    "description": "One target you touch of LV 4 or less collapses helplessly with disturbing, pained laughter for the spell's duration."
  },
  {
    "name": "Call to the Wild",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 12,
    "description": "Your voice carries the ancient melody that invites animals within far to come to your aid.\nThe GM decides which and how many arrive; the animals' combined LV being twice yours.\nThey begin arriving in one round and will take the most obvious, instinctual action that can assist you and your allies.\nThis chant does not grant you the ability to speak to them or control them."
  },
  {
    "name": "Carrion Stench",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Instant",
    "dc": 11,
    "description": "You exude the putrid essence of a ghast.\nOther living creatures within a near area centered on you make a CON check vs. your spellcasting check or suffer DISADV on attack rolls and spellcasting checks on their next turn.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Cast Out",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "You turn a creature aside, throwing it out of your presence. Choose a creature you can see. For the spell's duration, that creature can't come within near range of you. It can still attack you from outside of near range."
  },
  {
    "name": "Cat's Eye",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 12,
    "description": "Your irises grow to fill your eyes and your pupils turn into black, vertical slits. You can see invisible creatures and secret doors for the spell's duration."
  },
  {
    "name": "Cauldron",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Close",
    "duration": "1 round",
    "dc": 11,
    "description": "You conjure a bubbling cauldron next to you. It can produce one of the following effects:  Any broken mundane item placed inside the cauldron is repaired.\n\nA fat, croaking toad leaps out and follows your instructions for the next 3 rounds.\nYou can place up to 3 item slots of items inside the cauldron. The cauldron expels these items the next time you cast this spell (expelling items counts as the cauldron's single effect)."
  },
  {
    "name": "Chant",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Focus",
    "dc": 11,
    "description": "You begin an unearthly chant that lifts your vision beyond its ordinary limitations. For the spell's duration, you can see all invisible and hidden things as though they were plainly visible. This spell does not allow you to see in a way that you could not normally, such as in darkness or through walls."
  },
  {
    "name": "Charm Animals",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "1 day",
    "dc": 12,
    "description": "You magically beguile a number of natural animals whose combined LV equals your level.\nTwo LV 0 animals count as 1 LV.\nThey regard you as their master, but if they cannot understand you, they will take the most obvious, instinctual action to assist you.\nThe spell ends if you or your allies cause harm to any of them.\nThe animals return to their wild form when the spell ends."
  },
  {
    "name": "Charm Person",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "1d8 days",
    "dc": 11,
    "description": "You magically beguile one humanoid of LV 2 or less within near range, who regards you as a friend for the duration. The spell ends if you or your allies do anything harmful to the target. The target knows it was magically charmed after the spell ends."
  },
  {
    "name": "Chilling Shriek",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Instant",
    "dc": 13,
    "description": "With a ghastly scream, you terrify living creatures.\nLiving creatures LV 9 or less within near of you must make a CON check vs. your spellcasting check or flee from you for 1d4 rounds.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Cleansing Weapon",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 12,
    "description": "One weapon you touch is wreathed in purifying flames. It deals an additional 1d4 radiant damage (1d6 vs. undead) for the duration. [Note: FG effects can only add more dice for a given monster type, not substitute dice. Therefore, the effect has been changed to 1d4, plus an additional 1d2 vs. undead.]\n\nImage: Cleansing Weapon",
    "damage": "1d4",
    "damageType": "radiant"
  },
  {
    "name": "Cloak of Night",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Duration: 8 rounds",
    "duration": "Cloak of the Night|AC: 5|8 rounds|self|",
    "dc": 14,
    "description": "Range: Self"
  },
  {
    "name": "Cloudkill",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "5 rounds",
    "dc": 14,
    "description": "A putrid cloud of yellow poison fills a near-sized cube within range. It spreads around corners. Creatures inside the cloud are blinded and take 2d6 poison damage at the beginning of their turns. A creature of LV 9 or less that ends its turn fully inside the cloud dies.",
    "damage": "2d6",
    "damageType": "poison"
  },
  {
    "name": "Command",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "You issue a verbal command to one creature in range who can understand you. The command must be one word, such as \"kneel.\" The target obeys the command for as long as you focus. If your command is ever directly harmful to the creature, it may make a Charisma check vs. your last spellcasting check. On a success, the spell ends.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "charisma"
  },
  {
    "name": "Commune",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Instant",
    "dc": 14,
    "description": "You seek your god's counsel. Ask the GM up to three yes or no questions. The GM truthfully answers \"yes\" or \"no\" to each. If you cast this spell more than once in 24 hours, treat a failed spellcasting check for it as a critical failure instead."
  },
  {
    "name": "Companion Sight",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 14,
    "description": "You close your eyes and match your breath to that of your companion. You can see through the eyes of your animal companion."
  },
  {
    "name": "Confusion",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You mesmerize one creature you can see in range. The target can't take actions, and it moves in a random direction on its turn. If the target is LV 9+, it may make a Wisdom check vs. your last spellcasting check at the start of its turn to end the spell.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Conjure Wraith",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "1 day",
    "dc": 15,
    "description": "You touch a fresh corpse to conjure a vengeful wraith to do your bidding. It acts on your turn. After 1 day it turns to dust.\nOnly one instance of this spell may be active at a time. A second casting while another instance exists immediately destroys the previous wraith, even if the spell fails. The wraith counts against the LV limit of other undead minions.\nA critical fail on casting this spell still conjures the wraith, but it turns against you. Do not roll on the mishap table."
  },
  {
    "name": "Control Water (Priest)",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 14,
    "description": "You move and shape water. You can cause a section of water up to 100 feet in width and depth to change shape, defy gravity, or flow in a different direction.\n\nImage: Control Water"
  },
  {
    "name": "Control Water (Wizard)",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Far",
    "duration": "Focus",
    "dc": 14,
    "description": "You move and shape water. You can cause a section of water up to 100 feet in width and depth to change shape, defy gravity, or flow in a different direction.\n\nImage: Control Water"
  },
  {
    "name": "Corpse Companion",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "10 rounds",
    "dc": 11,
    "description": "A mostly intact corpse you touch rises as an animated lesser skeleton. You control this undead minion and it acts on your turn.\nOnly one instance of this spell may be active at a time. A 2nd casting of this spell destroys the previous minion, even if the spell fails. This does not count against your minion LV limit."
  },
  {
    "name": "Corpse Explosion",
    "tier": 3,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "With a clench of your fist, you cause a non-animated corpse to explode. All creatures within close of the corpse take 5d6 piercing damage.\n\nImage: Corpse Explosion",
    "damage": "5d6",
    "damageType": "piercing"
  },
  {
    "name": "Coven",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Instant",
    "dc": 13,
    "description": "You call upon the magic you share with your fellow witches. You regain the use of one tier 3 spell or lower that you can no longer cast for the day. After successfully casting this spell, you can't do so again until you complete a rest."
  },
  {
    "name": "Create Corpse",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 12,
    "description": "With the snap of your fingers, a fresh human corpse materializes at your feet."
  },
  {
    "name": "Create Tree",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1 day",
    "dc": 11,
    "description": "You lift your hands and a climbable tree grows from any flat, horizontal surface, up to 50' high. It lasts for one day before dematerializing and returning to the forest."
  },
  {
    "name": "Create Undead",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "1 day",
    "dc": 15,
    "description": "You conjure a vengeful undead creature to do your bidding. When you cast this spell, you choose to summon either a wight or wraith. It appears next to you and is under your control.  The undead creature acts on your turn. After 1 day, it melts away into smoke."
  },
  {
    "name": "Cure Wounds",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "spellType": "Healing",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "Your touch restores ebbing life. Roll a number of d6s equal to 1 + half your level (rounded down). One target you touch regains that many hit points.",
    "healing": "1d6",
    "levelScaling": "half",
    "multiplier": 1
  },
  {
    "name": "Curse",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Permanent",
    "dc": 14,
    "description": "A creature you touch is afflicted by one of the following curses:\n\nHideous boils and warts\nAll food tastes of ash\nVoice becomes shrill\nDisturbing nightmares\nAlways lose at gambling\nAn ally turns into an enemy\nFear of something ordinary"
  },
  {
    "name": "Dance of the Drum",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "Your captivating drumming compels enemies within a near distance from you to dance in revelry unless they pass a WIS check vs. your last chanting check. The effect on one target ends if you or your allies do anything the target notices to injure it."
  },
  {
    "name": "Dark Step",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Instant",
    "dc": 12,
    "description": "Starting from darkness, you teleport to a far distance that is also shrouded in darkness."
  },
  {
    "name": "Darkness",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You magically create darkness in a near cube within the spell's range. Creatures inside the area are blinded and they are obscured from view from the outside. Only magical light can penetrate this magical darkness."
  },
  {
    "name": "Deadlight",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "1 hour real time",
    "dc": 11,
    "description": "You use the soul of a fresh corpse to create a floating light that bobs in the air and casts illumination to a near distance around it. It can float up to a near distance on your turn."
  },
  {
    "name": "Deafen",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You utter a divine censure, deafening one creature you can see in range. The creature has disadvantage on tasks requiring listening."
  },
  {
    "name": "Deathward Circle",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Focus",
    "dc": 13,
    "description": "You conjure a circle of bones and necrotic energy out to near-sized cube centered on yourself. For the spell's duration, undead creatures LV 9 or less cannot attack or cast a hostile spell on anyone inside the circle.\nThe undead also can't possess, compel, or beguile anyone inside the circle.\n\nImage: Death Ward Circle"
  },
  {
    "name": "Demonic Possession",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 14,
    "description": "Your soul willingly leaves your body and occupies a target in near range. An unwilling target can make a CHA check vs. your spellcasting check. If they fail, you control the target on your turn, but your body stands in a helpless daze.\nWhen the target returns to its body, it becomes aware of the possession, but retains no memories of what transpired.\nIf the target dies, you return to your body as long as it is within near range, otherwise you return to your body and drop to 0 HP.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "cha"
  },
  {
    "name": "Detect Magic",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You can sense the presence of magic within near range for the spell's duration. If you focus for two rounds, you discern its general properties. Full barriers block this spell."
  },
  {
    "name": "Detect Thoughts",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You peer into the mind of one creature you can see within range. Each round, you learn the target's immediate thoughts. On its turn, the target makes a Wisdom check vs. your last spellcasting check. If the target succeeds, it notices your presence in its mind and the spell ends.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Dimension Door",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Instant",
    "dc": 14,
    "description": "You teleport yourself and up to one other willing creature to any point you can see."
  },
  {
    "name": "Disintegrate",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "A green ray shoots from your finger and turns a creature or object into ash. A target creature of LV 5 or less instantly dies. If it is LV 6+, it takes 3d8 force damage instead. A non-magical object up to the size of a large tree is destroyed.",
    "damage": "3d8",
    "damageType": "force"
  },
  {
    "name": "Dispel Magic",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "End one spell that affects one target you can see in range."
  },
  {
    "name": "Divination",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Instant",
    "dc": 14,
    "description": "You throw the divining bones or peer into the blackness between the stars, seeking a portent. You can ask the GM one yes or no question. The GM truthfully answers \"yes\" or \"no.\" If you cast this spell more than once in 24 hours, treat a failed spellcasting check for it as a critical failure, instead."
  },
  {
    "name": "Divine Intervention",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "Your revered ancestors intercede on your behalf. All allies within a near distance from you may reroll 1s, keeping the second result.\nThis applies to all rolls, including damage dice."
  },
  {
    "name": "Divine Vengeance",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 15,
    "description": "You become the divine avatar of your god's wrath, wreathed in holy flames or a black aura of smoldering corruption. For the spell's duration, you can fly a near distance, your weapons are magical, and you have a +4 bonus to your weapon attacks and damage."
  },
  {
    "name": "Domain of Fatigue",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Focus",
    "dc": 14,
    "description": "You cause a wilting of vitality for the living. All living creatures a near distance from you must make a CON check vs. your spellcasting check or have DISADV on all attacks and spellcasting checks.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Dominion",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 15,
    "description": "Mighty beings come to your aid. The beings must have a combined total of 16 levels or less. Chaotic PCs summon demons/devils, and lawful or neutral PCs summon angels. The beings act of free will to aid you on your turn. After 10 rounds, they return to their realms. You cannot cast this spell again until you complete penance."
  },
  {
    "name": "Drainblade",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Focus",
    "dc": 14,
    "description": "A melee weapon you touch becomes thirsty for blood. It now heals the wielder for an amount of HP equal to the damage it deals, encouraging the wielder to keep fighting."
  },
  {
    "name": "Dreamwalk",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You and any willing creatures you choose within close range step into the dream of a sleeping creature you name that is on your same plane. You and anyone travelling with you can step out of the creature, appearing next to it as if having teleported there."
  },
  {
    "name": "Enfeeble",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Instant",
    "dc": 15,
    "description": "A creature you touch has a random stat reduced to 3 (-4) for one week. Roll a d6 to determine which stat: 1. Strength, 2. Dexterity, 3. Constitution, 4. Intelligence, 5. Wisdom, 6. Charisma. If you fail the spellcasting check, you have a random stat reduced to 3 for a week, instead."
  },
  {
    "name": "Enlarge Animal",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Focus",
    "dc": 14,
    "description": "An animal you touch grows into the giant version of itself.\nReplace its stat block with its giant version."
  },
  {
    "name": "Evoke Rage",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1d4 rounds",
    "dc": 11,
    "description": "You call out the berserk rage locked inside someone. One willing humanoid you touch enters a berserk state. The target is immune to morale checks, has ADV on STR checks and melee attacks, and deals +1d4 damage for the spell's duration. If the target does not attack another creature on its turn, the spell ends."
  },
  {
    "name": "Eyebite",
    "tier": 1,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "One creature you target takes 1d4 psychic damage, and it can't see you until the end of its next turn.",
    "damage": "1d4",
    "damageType": "psychic"
  },
  {
    "name": "Fabricate",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 13,
    "description": "This spell can't target creatures. You turn a tree-sized collection of raw materials into a finished work. For example, you convert a pile of bricks or rocks into a bridge. The finished work converts back to raw materials when the spell ends."
  },
  {
    "name": "Fate",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 12,
    "description": "You painfully twist the golden threads of a creature's fate. One creature you target in range takes 1d10 psychic damage and loses any luck tokens it has.",
    "damage": "1d10",
    "damageType": "psychic"
  },
  {
    "name": "Feather Fall",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "Instant",
    "dc": 11,
    "description": "You may make an attempt to cast this spell when you fall. Your rate of descent slows so that you land safely on your feet."
  },
  {
    "name": "Finger of Death",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Instant",
    "dc": 15,
    "description": "One creature you touch of LV 9 or less dies. Treat a failed spellcasting check for this spell as a critical failure, and roll the mishap with disadvantage."
  },
  {
    "name": "Fireball",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 13,
    "description": "You hurl a small flame that erupts into a fiery blast. All creatures in a near-sized cube around where the flame lands take 4d6 fire damage.",
    "damage": "4d6",
    "damageType": "fire"
  },
  {
    "name": "Fireskull Familiar",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "1 hour real time",
    "dc": 12,
    "description": "You animate a single skull as your familiar from a corpse or skull you touch. The skull is bathed in blue flames and gifted with levitation-based flight.\nIt projects light in a near distance, it can speak and understand, retrieve small items, perform a minor bite attack, and deliver messages.\nOn your turn you can move it a near distance. See NPC entry for stats. Only one instance of this spell may be active at a time.\n\nImage: Fire Skull Familiar"
  },
  {
    "name": "Fixed Object",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 12,
    "description": "An object you touch that weighs no more than 5 pounds becomes fixed in its current location. It can support up to 5,000 pounds of weight for the duration of the spell."
  },
  {
    "name": "Flame Strike",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 14,
    "description": "You call down a holy pillar of fire, immolating one creature you can see within range. The target takes 2d6 fire damage.",
    "damage": "2d6",
    "damageType": "fire"
  },
  {
    "name": "Floating Disk",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 11,
    "description": "You create a floating, circular disk of force with a concave center. It can carry up to 20 gear slots. It hovers at waist level and automatically stays within near of you. It can't cross over drop-offs or pits taller than a human."
  },
  {
    "name": "Flora's Embrace",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 11,
    "description": "You awaken the spirits of the plants, entwining foes in a near area within range. Affected foes are reduced to moving a close distance on their turn. Targets can escape if they pass a STR check vs. your last chanting check."
  },
  {
    "name": "Fly",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "5 rounds",
    "dc": 13,
    "description": "Your feet lift from the ground, and you take to the air like a hummingbird. You can fly near for the spell's duration and are able to hover in place."
  },
  {
    "name": "Fog",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Close",
    "duration": "Focus",
    "dc": 11,
    "description": "A thick cloud of fog blooms in a close area around you, making you hard to see. The cloud moves with you. Attacks against creatures in the cloud have disadvantage."
  },
  {
    "name": "Foresee Good",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 12,
    "description": "You call on nature spirits to give you insights into a being's future. One humanoid you touch gains a luck token."
  },
  {
    "name": "Forest Altar",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You prepare a natural altar and commune with a nature spirit for 5 rounds. You gain answers to any question regarding an entire region, be they historical, navigational, or political."
  },
  {
    "name": "Forest Blend",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Focus",
    "dc": 14,
    "description": "In a forest environment, you and your allies turn invisible as long as they remain a near distance from you. The spell ends if you or your hidden allies attack or cast a spell.\nYou have advantage on focus checks for this spell."
  },
  {
    "name": "Forest of Treants",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 15,
    "description": "You call on the very essence of nature to bestow sentient life to up to 4 trees in a forest.\nThese trees become fully realized oak treants (see NPC entry) who will act friendly toward you and assist you with any task that does not involve the destruction of nature.\nThey return to their tree form when the spell ends."
  },
  {
    "name": "Fossilize",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Permanent",
    "dc": 13,
    "description": "Your touch hardens the bones of an undead minion, granting it +2 AC and imposing half damage on all damage dealt to it.\nFossilized undead cannot be healed.\nThis spell has no effect on already fossilized undead."
  },
  {
    "name": "Freya's Omen",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "1d6 rounds",
    "dc": 14,
    "description": "For the spell's duration, you do not lose the ability to cast a spell if you fail its spellcasting check. If you critically fail a spellcasting check, you may reroll your check once. You must use the new result."
  },
  {
    "name": "Frog Rain",
    "tier": 2,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 12,
    "description": "A rain of indignant frogs pelts a near-sized cube around a point you can see within range. All creatures within the frog rain take 1d6 blunt damage. Any surviving frogs hop away and disappear.",
    "damage": "1d6",
    "damageType": "blunt"
  },
  {
    "name": "Fungal Cloud",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "You lift your hands and a shrubsized mushroom grows from any surface and explodes, causing a cloud of spores to fill a nearsized cube.\nAll living creatures must make a CON check vs. your spellcasting check or have DISADV on attacks and checks for the next round.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Fury of the Spirits",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "The spirits favor your cause.\nYour allies roll double damage dice when successfully hitting enemies a near distance from you with physical attacks."
  },
  {
    "name": "Gaseous Form",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 13,
    "description": "You and your gear turn into a cloud of smoke for the spell's duration. You can fly and pass through any gap that smoke could. You can sense the terrain and any movement around you out to a near distance. You can't cast spells while in this form.\n\nImage: Gaseous Form"
  },
  {
    "name": "Ghost Shift",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Focus",
    "dc": 15,
    "description": "You and your equipment become incorporeal, allowing you to move in any direction, including up or down at half movement speed.\nYou can move through solid objects and cannot attack with or be struck by physical weapons. You may cast spells and spells can still affect you."
  },
  {
    "name": "Ghoulish Claws",
    "tier": 2,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 12,
    "description": "By your touch, a willing target's hands transform into ghastly claws. Your target may make a claw attack that deals 1d8 slashing damage plus paralyze.\nVictims hit by this attack must make a DC 12 CON check or be paralyzed for 1d4 rounds.",
    "damage": "1d8",
    "damageType": "slashing",
    "opposed": 1,
    "opposedDc": 12,
    "opposedAbility": "con"
  },
  {
    "name": "Giant Antapult",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Close",
    "duration": "Focus",
    "dc": 15,
    "description": "You wring your hands, conjuring a group of giant ants that form into a makeshift catapult capable of launching magical boulders at a single visible target within far. Make a ranged attack that deals 8d6 blunt damage on a successful hit.\n\nImage: Giant Antapult",
    "damage": "8d6",
    "damageType": "blunt"
  },
  {
    "name": "Gift of Air",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "Allies in a near area sprout majestic eagle wings, gifting them with flight. Allies can fly a near distance on their turn.\nYou have ADV on maintenance checks for this chant.\n\nImage: Gift of Air"
  },
  {
    "name": "Gift of Earth",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "Your resonating chant causes yourself and your allies in a near area centered on you to become rooted in place and grow barklike skin, giving a +2 AC bonus.\nYou and your allies are rooted in place until the chant ends."
  },
  {
    "name": "Gift of Fire",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You cause the wielded weapons of your allies in a near area centered on you to leap into flame. These weapons do an additional 1d4 fire damage.\nAny newly acquired or drawn weapons are set on fire for each round the chant continues.",
    "damage": "1d4",
    "damageType": "fire"
  },
  {
    "name": "Gift of Water",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "You call on the water spirits, who enchant all allies in a near area centered around you. As an action, they can unleash a forceful spray of water from an extended hand, dousing flames and pushing enemies straight back from close to near with a successful ranged attack roll.\nTargets can withstand the water spray by making a STR check vs. the attack roll."
  },
  {
    "name": "Glassbones",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Focus",
    "dc": 14,
    "description": "A creature you touch becomes fragile. It takes double damage for the spell's duration."
  },
  {
    "name": "Grace of the Gazelle",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You channel the spry spirit of the gazelle, granting you and your allies within a near distance from you advantage on all DEX checks and ranged attacks."
  },
  {
    "name": "Grasp from the Grave",
    "tier": 2,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Focus",
    "dc": 12,
    "description": "You call forth spectral hands that erupt from the ground in a near-sized cube of effect. All creatures in the cube move at half speed and take 1d4 blunt damage per round.\nOnce cast, the spectral hands cannot be moved.",
    "damage": "1d4",
    "damageType": "blunt"
  },
  {
    "name": "Grub Geyser",
    "tier": 3,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 13,
    "description": "You breathe a stream of flesheating maggots at a close target. Target must make a CON vs. your spellcasting check check or take 1d6 piercing damage and suffer DISADV on attacks and spellcasting checks as long as they are covered in maggots.\nTarget continues to take 1d6 piercing damage each round until the effect ends or until they take an action to brush them off.",
    "damage": "1d6",
    "damageType": "piercing"
  },
  {
    "name": "Hallucinate",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "One creature you target in near whose level is less than or equal to your own is overcome by visions of what might yet come to pass. For the spell's duration, the target cannot act on its turn unless it passes a Wisdom check equal to your last spellcasting check."
  },
  {
    "name": "Heal",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "One creature you touch is healed to full hit points. You cannot cast this spell again until you complete a rest."
  },
  {
    "name": "Healing Feast",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 15,
    "description": "You conjure a meal for all allies in near, healing 4d12 HP and curing poisoning and fatigue. It takes 5 rounds to eat and must be fully consumed."
  },
  {
    "name": "Healing Sprout",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 12,
    "description": "You lift one palm and a restoring plant springs from any surface within a near distance from you.\nConsuming the plant restores hit points equal to d6s determined by half of your level (rounded down), minimum 1d6."
  },
  {
    "name": "Heart of the Lion",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You bang your drum with ferocity, causing yourself and your allies within near range to gain unwavering determination.\nThose affected cannot be surprised and have advantage on initiative rolls."
  },
  {
    "name": "Hiss of the Viper",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You hiss loudly and rattle your shakers, calling on the spirit of the serpent.\nEnemies within a near area centered on you must make a DC 9 morale check at the beginning of their turn."
  },
  {
    "name": "Hold Monster",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "You paralyze one creature you can see within range. If the target is LV 9+, it may make a Strength check vs. your last spellcasting check at the start of its turn to end the spell.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "strength"
  },
  {
    "name": "Hold Person",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You magically paralyze one humanoid creature of LV 4 or less you can see within range."
  },
  {
    "name": "Hold Portal",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 11,
    "description": "You magically hold a portal closed for the duration. A creature must make a strength check vs. your spellcasting check to open the portal. The knock spell ends this spell.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "strength"
  },
  {
    "name": "Holy Weapon",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 11,
    "description": "One weapon you touch is imbued with a sacred blessing. The weapon becomes magical and has +1 to attack and damage rolls for the duration."
  },
  {
    "name": "Howl",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "All enemies within near range of you must immediately make a morale check. This spell does not affect creatures that are immune to morale checks."
  },
  {
    "name": "Hypnotize",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "One creature of LV 3 or less that can see you is rendered stupefied. Breaking the creature's line of sight to you allows it to make a DC 15 Charisma check. On a success, the spell ends.",
    "opposed": 1,
    "opposedDc": 15,
    "opposedAbility": "charisma"
  },
  {
    "name": "Illusion",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "You create a convincing visible and audible illusion that fills up to a near-sized cube in range. The illusion cannot cause harm, but creatures who believe the illusion is real react to it as though it were. A creature who inspects the illusion from afar must pass a Wisdom check vs. your last spellcasting check to perceive the false nature of the illusion. Touching the illusion also reveals its false nature.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Invisibility",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Close",
    "duration": "10 rounds",
    "dc": 12,
    "description": "A creature you touch becomes invisible for the spell's duration. The spell ends if the target attacks or casts a spell."
  },
  {
    "name": "Judgment",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 15,
    "description": "You instantly banish a creature you touch, sending it and all possessions it carries to face the judgment of your god. You can banish an intelligent creature of LV 10 or less. When the creature returns in 5 rounds, it has been healed to full hit points if its deeds pleased your god. It has been reduced to 1 hit point if its deeds angered your god. If your god can't judge its actions, it is unchanged."
  },
  {
    "name": "Killing Shriek",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "Instant",
    "dc": 15,
    "description": "You let out a mind-shattering scream that instantly slays any living beings who hear it. Living beings within near range of you must make a CON check vs. your spellcasting check or die. Creatures above LV 9 are immune.\n\nImage: Killing Shriek",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Knock",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Instant",
    "dc": 12,
    "description": "A door, window, gate, chest, or portal you can see within range instantly opens, defeating all mundane locks and barriers. This spell creates a loud knock audible to all within earshot."
  },
  {
    "name": "Know Intention",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "You instantly know the intention of a creature within range."
  },
  {
    "name": "Lay to Rest",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "An undead creature you touch, whose level is equal to or less than yours, is instantly sent to its final afterlife, reducing it to grave dust."
  },
  {
    "name": "Leaves in the Breeze",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "Your chant invokes a gentle breeze that slows the descent of you and your falling allies a near distance from you, allowing you to float safely to the ground."
  },
  {
    "name": "Levitate",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "Focus",
    "dc": 12,
    "description": "You can float a near distance vertically per round on your turn. You can also push against solid objects to move horizontally."
  },
  {
    "name": "Life Wolf",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Until allies are stable",
    "dc": 14,
    "description": "You summon a celestial life wolf (see NPC record) that acts on your turn. It spends its movement and actions to save your dying and unconscious allies, using one action to magically heal them to 1 HP.\nOnce no more allies are dying, the wolf vanishes.\nIf you drop to 0 HP while the life wolf is active, it will turn its attention to you, and vanish after saving you, even if others still require aid.\n\nImage: Life Wolf"
  },
  {
    "name": "Lifeward Circle",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "You conjure a circle of bones and necrotic energy out to nearsized cube centered on yourself.\nFor the spell's duration, enemy living creatures LV 9 or below cannot attack or cast a hostile spell on anyone inside the circle."
  },
  {
    "name": "Light (Priest)",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1 hour real time",
    "dc": 11,
    "description": "One object you touch glows with bright, heatless light, illuminating out to a near distance for 1 hour of real time."
  },
  {
    "name": "Light (Wizard)",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "1 hour real time",
    "dc": 11,
    "description": "One object you touch glows with bright, heatless light, illuminating out to a near distance for 1 hour of real time."
  },
  {
    "name": "Lightning Bolt",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 13,
    "description": "You shoot a blue-white ray of lightning from your hands, hitting all creatures in a straight line out to a far distance. Creatures struck by the lightning take 3d6 lightning damage.\n\nImage: Lightning Bolt",
    "damage": "3d6",
    "damageType": "lightning"
  },
  {
    "name": "Living Revelation",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 15,
    "description": "Your rhythmic chant pierces through walls and doors, revealing the presence of living creatures beyond. You can determine their approximate size, shape, and location.\nThe chant reveals creatures behind walls up to a far distance, but the sound cannot penetrate through double walls."
  },
  {
    "name": "Locate Corpse",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Self",
    "duration": "Instant",
    "dc": 11,
    "description": "You know the direction and range of the closest nonanimated corpse."
  },
  {
    "name": "Loki's Trickery",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 14,
    "description": "You are filled with Loki's hypnotic guile. Creatures who hear you speak will alter their own beliefs and memories to match your suggestion. Target one creature who can hear and understand you within range. You make one plausible statement, true or not. The target must make a Wisdom check vs. your last spellcasting check. If it fails, it now believes what you stated as though it were fact, regardless of what it knows.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Lure of the Piper",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "Your enchanting performance compels enemies in a near distance from you to abandon their actions and follow you obediently wherever you lead.\nBeguiled creatures make a WIS check vs. your last chanting check on their turn to resist.\nIf you stop moving, or if you or your allies actively harm a beguiled creature, the beguilement ends.\n\nImage: Lure of the Piper"
  },
  {
    "name": "Mage Armor",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 11,
    "description": "An invisible layer of magical force protects your vitals. Your armor class becomes 14 (18 on a critical spellcasting check) for the spell's duration. [Replace the X values in the effects with the correct amounts for success result and critical success result.]"
  },
  {
    "name": "Magic Circle",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "You conjure a circle of runes out to near-sized cube centered on yourself and name a type of creature (for example, demons). For the spell's duration, creatures of the chosen type cannot attack or cast a hostile spell on anyone inside the circle. The chosen creatures also can't possess, compel, or beguile anyone inside the circle."
  },
  {
    "name": "Magic Missile",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 11,
    "description": "You have advantage on your check to cast this spell. A glowing bolt of force streaks from your open hand, dealing 1d4 force damage to one target.\n\nImage: Magic Missile",
    "damage": "1d4",
    "damageType": "force"
  },
  {
    "name": "Mass Breathe Water",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "1 hour",
    "dc": 14,
    "description": "You and your allies a near distance from you gain magical gills, allowing you to breathe underwater for the spell's duration."
  },
  {
    "name": "Mass Cure",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "spellType": "Healing",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "All allies within near range of you regain 2d6 hit points.",
    "healing": "2d6"
  },
  {
    "name": "Mirror Image",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You create a number of illusory duplicates of yourself equal to half your level rounded down (minimum 1). The duplicates surround you and mimic you. Each time a creature attacks you, the attack misses and causes one of the duplicates to evaporate. If all of the illusions have disappeared, the spell ends."
  },
  {
    "name": "Mistletoe",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "1d8 days",
    "dc": 13,
    "description": "Two creatures you can see within near of you become enchanted with each other for 1d8 days. Each time one of the affected creatures takes damage, it may make a DC 15 Charisma check. On a success, the spell ends.",
    "opposed": 1,
    "opposedDc": 15,
    "opposedAbility": "charisma"
  },
  {
    "name": "Misty Step",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "Instant",
    "dc": 12,
    "description": "In a puff of smoke, you teleport a near distance to an area you can see."
  },
  {
    "name": "Moonbeam",
    "tier": 4,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 14,
    "description": "A wavering ray of silvery moonlight strikes one creature within far. It takes 3d6 radiant damage.",
    "damage": "3d6",
    "damageType": "radiant"
  },
  {
    "name": "Mortal Rejuvenation",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "Instant",
    "dc": 14,
    "description": "By reaping the last death energy from any fresh corpses within near, you and allies within near each regain 1d6 HP per corpse.\nThese corpses turn to grave dust."
  },
  {
    "name": "Mother of Night",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Instant",
    "dc": 15,
    "description": "You beseech the Mother of Night to lend you power."
  },
  {
    "name": "Mummify",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Permanent",
    "dc": 15,
    "description": "After a day of preparation and ceremony, you embalm a corpse as a mummy who acts on your turn. Mummies deteriorate when walking long distances, but can be carried in a casket or left to guard a location. This minion does not count against the LV limit of other raised undead minions."
  },
  {
    "name": "Nightmare",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "On the same plane",
    "duration": "Focus",
    "dc": 14,
    "description": "You visit the dreams of one sleeping creature, sending it heart-stopping nightmares. You can target a creature whose level is less than or equal to half your level rounded down (minimum 1). The target must be sleeping, and you must have seen it before in person. If you successfully focus on this spell for 3 rounds in a row, the creature dies of fright."
  },
  {
    "name": "Oak, Ash, Thorn",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 11,
    "description": "For the spell's duration, faeries, demons, and devils can't attack you. These beings also can't possess, compel, or beguile you."
  },
  {
    "name": "Obscuring Spores",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You create and cause a mushroom to explode, causing a dense cloud of obscuring spores to fill a near cube centered on a point within near. Creatures cannot see in or out of the cloud.\nIt lasts for the full duration or until a wind disperses it."
  },
  {
    "name": "Odin's Wisdom",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "1d6 rounds",
    "dc": 14,
    "description": "For the spell's duration, add your level as an additional bonus to your Wisdom checks and spellcasting checks."
  },
  {
    "name": "Pass Tree",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You teleport from a tree you touch to any tree that you have seen before."
  },
  {
    "name": "Passwall",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 14,
    "description": "A tunnel of your height opens in a barrier you touch and lasts for the duration. The passage can be up to near distance in length and must be in a straight line."
  },
  {
    "name": "Pillar of Salt",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "A creature you target turns into a statue made of hardened salt. You can target a creature you can see of LV 5 or less. If you successfully focus on this spell for 3 rounds in a row, the transformation becomes permanent."
  },
  {
    "name": "Pin Doll",
    "tier": 3,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "On the same plane",
    "duration": "Focus",
    "dc": 13,
    "description": "You pin a piece of hair or flesh taken from one creature to a small, burlap doll the spell conjures. On your turn while focusing on this spell, you can push a pin into the doll. Each time you do this, the creature who the hair or flesh belonged to takes 2d6 necrotic damage. After this spell ends, the piece of hair or flesh burns to ash.",
    "damage": "2d6",
    "damageType": "necrotic"
  },
  {
    "name": "Plane Shift (Priest)",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You fold space and time, transporting yourself and all willing creatures within close range to a location on another plane of your choice. Unless you have been to your intended location before, you appear in a random place on the destination plane.\n\nImage: Plane Shift"
  },
  {
    "name": "Plane Shift (Wizard)",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You fold space and time, transporting yourself and all willing creatures within close range to a location on another plane of your choice. Unless you have been to your intended location before, you appear in a random place on the destination plane.\n\nImage: Plane Shift"
  },
  {
    "name": "Plantidote",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "You cause a curative plant to spring from any surface within a near distance from you.\nConsuming the plant neutralizes poison, disease, or heals 1d4 stat damage.",
    "damage": "1d4",
    "damageType": "stat"
  },
  {
    "name": "Poison",
    "tier": 2,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "Touch",
    "duration": "5 rounds",
    "dc": 12,
    "description": "One worn or carried object you touch becomes toxic for the spell's duration. Any creature in contact with the object at the start of its turn takes 1d6 poison damage.",
    "damage": "1d6",
    "damageType": "poison"
  },
  {
    "name": "Polymorph",
    "tier": 4,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "10 rounds",
    "dc": 14,
    "description": "You transform a creature you touch into another natural creature you choose of equal or smaller size. Any gear the target carries melds into its new form. The target gains the creature's hit points, armor class, and attacks, but retains its intellect. If the target goes to 0 hit points, it reverts to its true form at half its prior hit points. You can target any willing creature with this spell, or an unwilling creature whose level is less than or equal to half your level rounded down (minimum 1)."
  },
  {
    "name": "Poseidon's Passage",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 14,
    "description": "You call on the water spirits to part the sea. You create passages or hold back the tide in a double near cube within the chant's range.\n\nImage: Poseidon's Passage"
  },
  {
    "name": "Potion",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "As a part of casting this spell, you must bless a single drink of any liquid. The liquid gains healing properties for 1 day. A creature who imbibes it may end the effects of one poison or may immediately stop dying (the creature remains at 0 HP)."
  },
  {
    "name": "Power Word Kill",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Instant",
    "dc": 15,
    "description": "You utter the Word of Doom. One creature you target of LV 9 or less dies if it hears you. Treat a failed spellcasting check for this spell as a critical failure, and roll the mishap with disadvantage."
  },
  {
    "name": "Prevent Decay",
    "tier": 2,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "1 day",
    "dc": 12,
    "description": "Any corpse you touch is perfectly preserved for 1 day."
  },
  {
    "name": "Primal Purge",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "You compel your allies within near range from you to expel any curses, enchantments, possessions, poisons, diseases, or parasites."
  },
  {
    "name": "Prismatic Orb (Cold)",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "You send a strobing orb of energy streaking toward a target within range. Choose an energy type from fire, cold, or electricity. Adjust the damage type if needed. The orb deals 3d8 cold damage and delivers a concussive blast of the chosen energy type. If the energy type is anathema to the target's existence (for example, cold energy against a fire elemental), the orb deals double damage to it instead.",
    "damage": "3d8",
    "damageType": "cold"
  },
  {
    "name": "Prismatic Orb (Electricity)",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "You send a strobing orb of energy streaking toward a target within range. Choose an energy type from fire, cold, or electricity. Adjust the damage type if needed. The orb deals 3d8 lightning damage and delivers a concussive blast of the chosen energy type. If the energy type is anathema to the target's existence (for example, cold energy against a fire elemental), the orb deals double damage to it instead.",
    "damage": "3d8",
    "damageType": "lightning"
  },
  {
    "name": "Prismatic Orb (Fire)",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "You send a strobing orb of energy streaking toward a target within range. Choose an energy type from fire, cold, or electricity. Adjust the damage type if needed. The orb deals 3d8 fire damage and delivers a concussive blast of the chosen energy type. If the energy type is anathema to the target's existence (for example, cold energy against a fire elemental), the orb deals double damage to it instead.",
    "damage": "3d8",
    "damageType": "fire"
  },
  {
    "name": "Prophecy",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Instant",
    "dc": 15,
    "description": "You commune directly with your god for guidance. Ask the GM one question. The GM answers the question truthfully using the knowledge your god possesses. Deities are mighty, but not omniscient. You cannot cast this spell again until you complete penance."
  },
  {
    "name": "Protection from Energy",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "Focus",
    "dc": 13,
    "description": "One creature you touch becomes impervious to the wild fury of the elements. Choose fire, cold, or electricity. For the spell's duration, the target is immune to harm from energy of the chosen type."
  },
  {
    "name": "Protection from Evil (Priest)",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Focus",
    "dc": 11,
    "description": "For the spell's duration, chaotic beings have disadvantage on attack rolls and hostile spellcasting checks against the target. These beings also can't possess, compel, or beguile it. When cast on an already-possessed target, the possessing entity makes a Charisma check vs. the last spellcasting check. On a failure, the entity is expelled. [After applying the effect, target this effect only at Chaotic beings by holding down SHIFT and dragging the effect to those beings.]\n\nImage: Protection from Evil",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "charisma"
  },
  {
    "name": "Protection from Evil (Wizard)",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "Focus",
    "dc": 11,
    "description": "For the spell's duration, chaotic beings have disadvantage on attack rolls and hostile spellcasting checks against the target. These beings also can't possess, compel, or beguile it. When cast on an already-possessed target, the possessing entity makes a Charisma check vs. the last spellcasting check. On a failure, the entity is expelled. [After applying the effect, target this effect only at Chaotic beings by holding down SHIFT and dragging the effect to those beings.]\n\nImage: Protection from Evil",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "charisma"
  },
  {
    "name": "Protection from Undead",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 11,
    "description": "The undead have DISADV on attack rolls against a target you touch. These beings also can't possess, compel, or beguile it.\nWhen cast on an already-possessed target, the possessing entity makes a CHA check vs. the last spellcasting check. On a failure, the entity is expelled.\n\nImage: Protection from Undead",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "cha"
  },
  {
    "name": "Puppet",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Focus",
    "dc": 11,
    "description": "One humanoid creature of LV 2 or less you touch becomes ensnared by your movements. On your turn, the creature mimics all your movements. If mimicking you would cause the creature to directly harm itself or an ally, it can make a DC 15 Charisma check. On a success, it resists mimicking you.",
    "opposed": 1,
    "opposedDc": 15,
    "opposedAbility": "charisma"
  },
  {
    "name": "Ragnarok",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "You look deep into the strands of fate, learning the final destiny of one soul after the battle of Ragnarok. Do they live, or die? Choose one creature in range. You can only target the same creature with this spell one time. That creature must pass a CON check equal to your spellcasting check or die instantly."
  },
  {
    "name": "Raise Dead",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "1 day",
    "dc": 13,
    "description": "You utter an incantation and all corpses within a near range rise as zombies or skeletons under your control, their condition dictating which form they take.\nThey maintain the shape, HP and LV they had in life, but lose all of their abilities. They gain the attacks, stats, and abilities of skeletons or zombies.\nThey act on your turn, then collapse to dust after 1 day.\nThe combined LV of controlled undead cannot exceed yours.\nRecasting adds to or replaces previous undead minions with new ones until the LV limit is reached. (See pg. 41 for more information.)\nA critical fail causes all undead minions under your control to collapse into grave dust including undead gained from other spells or the Claim Undead ability. Do not roll on the mishap table."
  },
  {
    "name": "Raven",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Unlimited",
    "duration": "Instant",
    "dc": 13,
    "description": "You whisper a message to Odin's own ravens, and they carry it across all worlds to its recipient. Speak a short sentence, and the name of its recipient, dead or alive. That creature hears your utterance whispered in its mind."
  },
  {
    "name": "Read the Runes",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Instant",
    "dc": 12,
    "description": "You ask the gods a question and cast the runestones, interpreting the meaning of the results. Ask the Game Master one yes or no question. The Game Master truthfully answers \"yes\" or \"no.\"\n\nImage: Read the Runes"
  },
  {
    "name": "Rebuke Unholy",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "You rebuke creatures who oppose your alignment, forcing them to flee. You must present a holy symbol to cast this spell. If you are lawful or neutral, this spell affects demons, devils, and outsiders. If you are chaotic, this spell affects angels and natural creatures of the wild. Affected creatures within near of you must make a charisma check vs. your spellcasting check. If a creature fails by 10+ points and is equal to or less than your level, it is destroyed. Otherwise, on a fail, it flees from you for 5 rounds.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "charisma"
  },
  {
    "name": "Reduce Animal",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 12,
    "description": "An animal you touch is reduced to a quarter of its normal size and any giant animal is reduced to normal size. If animal is engaged in combat, it makes a morale check.\n\nImage: Reduce Animal"
  },
  {
    "name": "Regenerate",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "spellType": "Healing",
    "range": "Close",
    "duration": "Focus",
    "dc": 14,
    "description": "A creature you touch regains 1d4 hit points on your turn for the duration. This spell also regrows lost body parts.",
    "healing": "1d4"
  },
  {
    "name": "Remove Curse",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 14,
    "description": "With the touch of your hands, you expunge one curse of your choice affecting the target."
  },
  {
    "name": "Resilient Sphere",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "5 rounds",
    "dc": 14,
    "description": "You conjure a weightless, glassy sphere around you that extends out to close range. For the spell's duration, nothing can pass through or crush the sphere. You can roll the sphere a near distance on your turn.\n\nImage: Resilient Sphere"
  },
  {
    "name": "Restoration",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "With the touch of your hands, you expunge curses and illnesses. One curse, illness, or affliction of your choice affecting the target creature ends."
  },
  {
    "name": "Resurrect Animal",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 14,
    "description": "You bring a natural animal you touch back to life from the dead.\nThe animal must have been dead for no more than the number of days equal to half your level rounded up."
  },
  {
    "name": "Rock to Mud",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "With your touch, you transmute up to a near-cube area of contiguous rock into mud."
  },
  {
    "name": "Sacred Bounty",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 11,
    "description": "You open yourself to the abundance of the universe.\nWhen you or your allies discover treasure worth at least 1 XP, you gain an additional +1 XP."
  },
  {
    "name": "Sacrifice",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Touch",
    "duration": "Instant",
    "dc": 12,
    "description": "As a part of casting this spell, you must ritually sacrifice a living creature of LV 2 or higher. The target you touch gains a bonus to their next check or attack roll equal to the sacrificed creature's level."
  },
  {
    "name": "Scrying",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 15,
    "description": "You look into a crystal ball or reflecting pool, calling up images of a distant place. For the spell's duration, you can see and hear a creature or location you choose that is on the same plane. This spell is DC 18 to cast if you try to scry on a creature or location that is unfamiliar to you. Each round, creatures you view may make a Wisdom check vs. your last spellcasting check. On a success, they become aware of your magical observation.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "wisdom"
  },
  {
    "name": "Sending",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Unlimited",
    "duration": "Instant",
    "dc": 13,
    "description": "You send a brief, mental message to any creature with whom you are familiar who is on the same plane."
  },
  {
    "name": "Shadowdance",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "3 rounds",
    "dc": 11,
    "description": "You spin shadowstuff into a convincing visible and audible illusion at a point within near. The illusion can be as big as a person and can move within a near range of where it appeared. The illusion can't affect physical objects. Touching the illusion reveals its false nature."
  },
  {
    "name": "Shamanic Purge",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You compel your allies within a near range from you to undergo a violent upheaval, expelling possessing spirits, compulsions, or beguilements."
  },
  {
    "name": "Shaman's Shield",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "Harnessing the power of ancestral shamans, you conjure a shield of protection against hostile magic out to a near-sized cube centered on yourself. Such magic cannot affect or harm you or your allies inside the shield."
  },
  {
    "name": "Shapechange",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 15,
    "description": "You transform yourself and any gear you carry into another natural creature you've seen of level 10 or less. You gain the creature's hit points, armor class, and attacks, but retain your intellect. If you go to 0 hit points while under the effects of this spell, you revert to your true form at 1 hit point."
  },
  {
    "name": "Shell of the Turtle",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You invoke the tortoise's protective energies, granting you and your allies within a near range a +1 bonus to AC."
  },
  {
    "name": "Shield of Faith",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "5 rounds",
    "dc": 11,
    "description": "A protective force wrought of your holy conviction surrounds you. You gain a +2 bonus to your armor class for the duration."
  },
  {
    "name": "Silence",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Far",
    "duration": "Focus",
    "dc": 12,
    "description": "You magically mute sound in a near cube within the spell's range. Creatures inside the area are deafened, and any sounds they create cannot be heard."
  },
  {
    "name": "Sleep",
    "tier": 1,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "You weave a lulling spell that fills a near-sized cube extending from you. Creatures in the area of effect fall into a deep sleep if they are LV 2 or less. Vigorous shaking or being injured wakes them."
  },
  {
    "name": "Smite",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 12,
    "description": "You call down punishing flames on a creature you can see within range. It takes 1d6 fire damage.",
    "damage": "1d6",
    "damageType": "fire"
  },
  {
    "name": "Song of Immunity",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You name a damage type, and you and your allies within a near area from you become immune to that type of damage.\nPossible damage types are: acid, cold, fire, force, lightning, necrotic, poison, psychic, radiant, blunt, piercing, and slashing. You may grant immunity to disease instead of a damage type. This spell does not grant general magic immunity or physical damage immunity. [Adjust the effect as needed.]"
  },
  {
    "name": "Song of Resistance",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "You name a damage type, and you and your allies within a near area from you receive half damage from that type of damage.\nPossible damage types are: acid, cold, fire, force, lightning, necrotic, poison, psychic, radiant, blunt, piercing, and slashing. [Adjust the effect as needed.]"
  },
  {
    "name": "Soul Jar",
    "tier": 5,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Permanent",
    "dc": 15,
    "description": "You transfer the soul of one creature you touch of LV 9 or less into a vessel, such as a jar. The creature's body becomes comatose, but it doesn't die. If the vessel opens or breaks, the creature's soul returns to its body. You can possess the empty body with your own spirit, taking control of it. Your body becomes comatose during this time. If the body dies while you possess it, your soul returns to your body."
  },
  {
    "name": "Soul Reap",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "Your hand goes incorporeal as you reach into the body of a LV 9 or lower target and pull out its soul, causing the target to die.\nTreat a failed spellcasting check as a critical failure."
  },
  {
    "name": "Soulbind",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Focus",
    "dc": 12,
    "description": "You seal the soul of a living creature, preventing magic from leeching into it. One creature you touch becomes nearly impervious to all magic. For the spell's duration, all other spells targeting the creature (harmful or helpful) are DC 18 to cast. This spell ends as soon as the target is affected by another spell."
  },
  {
    "name": "Speak with Animals",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "5 rounds",
    "dc": 11,
    "description": "You are able to speak to and understand all types of animals for the duration of the spell."
  },
  {
    "name": "Speak with Dead",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "A dead body you touch answers your questions. You can ask up to three yes or no questions (one at a time). The corpse truthfully answers \"yes\" or \"no\" to each.\nIf you cast this spell more than once in 24 hours, treat a failed spellcasting check for it as a critical failure, instead."
  },
  {
    "name": "Speak with Dead (Priest)",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "A dead body you touch answers your questions in a distant, wheezing voice. You can ask the dead body up to three yes or no questions (one at a time). The GM truthfully answers \"yes\" or \"no\" to each. If you cast this spell more than once in 24 hours, treat a failed spellcasting check for it as a critical failure instead.\n\nImage: Speak with Dead"
  },
  {
    "name": "Speak with Dead (Wizard)",
    "tier": 3,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "A dead body you touch answers your questions in a distant, wheezing voice. You can ask the dead body up to three yes or no questions (one at a time). The GM truthfully answers \"yes\" or \"no\" to each. If you cast this spell more than once in 24 hours, treat a failed spellcasting check for it as a critical failure instead.\n\nImage: Speak with Dead"
  },
  {
    "name": "Spectral Drain",
    "tier": 4,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 14,
    "description": "Partially transparent black tendrils erupt from your hands and engulf an undead creature, dealing 3d6 radiant damage and healing you by the same amount.\n\nImage: Spectral Drain",
    "damage": "3d6",
    "damageType": "radiant"
  },
  {
    "name": "Spectral Scythe",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "10 turns",
    "dc": 11,
    "description": "You manifest a ghostly image of a scythe floating within near.\nOn your turn you can move it up to near distance away and cast touch spells through it. Apply your Necrotic Reap bonus to spells cast in this way.\nOnly one instance of this spell may be active at a time.\nA second casting of this spell while another instance exists immediately de-activates the previous one, even if the spell fails."
  },
  {
    "name": "Spidersilk",
    "tier": 2,
    "castingAttribute": "Charisma",
    "range": "Self",
    "duration": "Focus",
    "dc": 12,
    "description": "Sticky spidersilk covers your hands and feet. For the spell's duration, you can walk on vertical surfaces as easily as if it were flat ground."
  },
  {
    "name": "Stoneskin",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 14,
    "description": "Your skin becomes like granite. For the spell's duration, your armor class becomes 17 (20 on a critical spellcasting check). [Replace the X values in the effects with the correct amounts for success result and critical success result.]"
  },
  {
    "name": "Summon Animal",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 13,
    "description": "You invoke your nature spirit, summoning a natural animal of LV 5 or less, arriving in one round. During that round, you do not have to make a focus check. The animal is under your control and acts on your turn. If you lose focus on this spell, you lose control of the animal and it acts instinctually based on the situation.\nIt cannot communicate with you unless you know its language."
  },
  {
    "name": "Summon Extraplanar",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "You reach into the outer planes, summoning forth a creature. You summon an elemental or outsider of LV 7 or less. The creature is under your control and acts on your turn. If you lose focus on this spell, you lose control of the creature and it becomes hostile toward you and your allies. You must pass a spellcasting check on your turn to return the creature to the outer planes."
  },
  {
    "name": "Summon Giant Animal",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 15,
    "description": "By invoking the earth spirits, you summon a giant animal of LV 9 or less, arriving in one round. During that round, you don't have to make a focus check. The animal is under your control and acts on your turn.\nIf you lose focus on this spell, you lose control of the animal and it acts instinctually based on the situation. It cannot communicate with you unless you know its language.\n\nImage: Summon Giant Animal"
  },
  {
    "name": "Summon Mummy",
    "tier": 4,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 14,
    "description": "You summon forth a mummy from its hidden crypt who will act on your turn and do your bidding. After 5 rounds, it melts away into smoke.\nA critical fail on casting this spell still summons the mummy, but it turns against you during its 5 round lifespan. Do not roll on the mishap table."
  },
  {
    "name": "Summon Wraith",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 13,
    "description": "You summon forth a wraith from the incorporeal plane who will act on your turn and do your bidding. After 5 rounds, it melts away into smoke.\nA critical fail on casting this spell still summons the wraith, but it turns against you during its 5 round lifespan.\nDo not roll on the mishap table."
  },
  {
    "name": "Swarm",
    "tier": 3,
    "castingAttribute": "Charisma",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "A dense swarm of biting bats, rats, or locusts appears in a nearsized cube around a point you can see within range. All creatures that start their turn within the swarm take 2d6 piercing damage and are blinded.",
    "damage": "2d6",
    "damageType": "piercing"
  },
  {
    "name": "Sword of Scorpions",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "You clap your hands, conjuring a swarm of scorpions that form into a living scimitar that hits a close opponent, dealing 1d12 slashing damage. Damaged target must make a CON check vs. your spellcasting check or be paralyzed for 1d4 rounds.\n\nImage: Sword of Scorpions",
    "damage": "1d12",
    "damageType": "slashing",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "con"
  },
  {
    "name": "Telekinesis",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Far",
    "duration": "Focus",
    "dc": 14,
    "description": "You lift a creature or object with your mind. Choose a target that weighs 1,000 pounds or less. You can move it a near distance in any direction and hold it in place."
  },
  {
    "name": "Teleport",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Close",
    "duration": "Instant",
    "dc": 15,
    "description": "You and any willing creatures you choose within close range teleport to a location you specify on your same plane. You can travel to a known teleportation sigil or to a location you've been before. Otherwise, you have a 50% chance of arriving off-target."
  },
  {
    "name": "The Sky Is Falling",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "Your spirit guides deem your enemies unworthy, releasing falling debris from the sky to strike them in a near area centered on you.\nAll enemies must make a DEX check vs. your last chanting check or take 2d6 blunt damage.",
    "damage": "2d6",
    "damageType": "blunt"
  },
  {
    "name": "Thor's Thunder",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 14,
    "description": "Thor casts down a bolt of lightning to strike one target. The target takes 3d6 lightning damage.\n\nImage: Thor's Thunder",
    "damage": "3d6",
    "damageType": "lightning"
  },
  {
    "name": "Thrall Offering",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 11,
    "description": "You magically beguile one humanoid of LV 2 or less within near range who is compelled to approach one of your undead minions and allow itself to be attacked with advantage.\nIf there are no undead minions, the affected humanoid follows you to the best of its ability until the spell ends or an undead minion appears.\nIf a minion kills it, that undead is healed to full HP. The spell ends if you or your allies hurt it, but stays beguiled if a minion attacks it. The target remembers you magically enchanted it."
  },
  {
    "name": "Timber",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Far",
    "duration": "Instant",
    "dc": 15,
    "description": "With a simple hand motion, you topple a tree, dealing 4d8 blunt damage to any creatures along a near-length line where base of the tree falls.",
    "damage": "4d8",
    "damageType": "blunt"
  },
  {
    "name": "Toadstool",
    "tier": 2,
    "castingAttribute": "Charisma",
    "spellType": "Healing",
    "range": "Self",
    "duration": "Instant",
    "dc": 12,
    "description": "You conjure a plump, speckled toadstool in your hand. It disappears at the end of your next turn. A creature that eats the toadstool regains 1d6 hit points.",
    "healing": "1d6"
  },
  {
    "name": "Tomorrow's Vision",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Instant",
    "dc": 14,
    "description": "After one round of communing with your nature spirit, you can ask three yes/no questions to gain insight into an important event or events that are expected to happen in the next week.\nYour role in the event cannot be predicted, only what is likely to occur in the world or region.\nThe questions cannot directly pertain to you or your allies."
  },
  {
    "name": "Torch of Fireflies",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1 hour real time",
    "dc": 11,
    "description": "You summon a cloud of fireflies that illuminate out to a near distance for 1 hour of real time.\nThe fireflies follow the ovate around. Treat them as a LV 0 animal with 1 HP.\nThey are attracted to any invisible creatures, revealing their position if encountered."
  },
  {
    "name": "Touch of Fatigue",
    "tier": 1,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "1 round",
    "dc": 11,
    "description": "A target you touch becomes exhausted during its next turn, giving it disadvantage on attacks and spellcasting checks."
  },
  {
    "name": "Train Animal",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 13,
    "description": "After 10 rounds of concentration, you train any calmed or captive animal to become your companion. This companionship mirrors the class talent of the same name."
  },
  {
    "name": "Trance",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "You enter a trance, catching small glimpses of a creature's fate. One humanoid creature you touch (you can't target yourself) gains a luck token. It can't have more than one luck token at once."
  },
  {
    "name": "Tree Assault",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You cause a tree within near to use its limbs to lash at close opponents. Use your own STR or DEX to make one melee slam attack per round, dealing 2d6 blunt damage.",
    "damage": "2d6",
    "damageType": "blunt"
  },
  {
    "name": "Tree Guardian",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 11,
    "description": "You turn a tree within near into a guardian for you and your allies. Allies within close will be protected by the tree's branches, gaining +2 to their AC."
  },
  {
    "name": "Tree Sanctuary",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "1 rest",
    "dc": 14,
    "description": "After 2 rounds of concentration, a portal appears on a tree you touch. Stepping inside seals the portal, transporting you to a secure chamber within the tree.\nWhile inside, you are aware of, but protected from, external influences. You are expelled from the chamber if the tree is destroyed.\nYou may complete a rest while safely in this chamber. Once your rest is complete, you reemerge from the tree.\nNo one else can pass through the tree portal."
  },
  {
    "name": "Turn Undead",
    "tier": 1,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "You rebuke undead creatures, forcing them to flee. You must present a holy symbol to cast this spell. Undead creatures within near of you must make a charisma check vs. your spellcasting check. If a creature fails by 10+ points and is equal to or less than your level, it is destroyed. Otherwise, on a fail, it flees from you for 5 rounds.",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "charisma"
  },
  {
    "name": "Undeath Blessing",
    "tier": 1,
    "castingAttribute": "Constitution",
    "spellType": "Healing",
    "range": "Close",
    "duration": "Instant",
    "dc": 11,
    "description": "Your touch infuses necrotic vitality to an undead or withers the living.\nThe undead target you touch regains 1d6 hit points, while a living target you touch takes 1d6 necrotic damage instead.",
    "damage": "1d6",
    "damageType": "necrotic",
    "healing": "1d6"
  },
  {
    "name": "Undeath to Dust",
    "tier": 5,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 15,
    "description": "You wave your arm and all undead less than LV 9 in a near area around you instantly turn to dust. Undead LV 9 or above take 4d6 radiant damage.\n\nImage: Undeath to Dust",
    "damage": "4d6",
    "damageType": "radiant"
  },
  {
    "name": "Unhallowed Ground",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Far",
    "duration": "Permanent",
    "dc": 15,
    "description": "You imbue a landmark with profane magic, permanently turning it into a necromantic beacon of undeath.\nAll the land within 6 miles of the beacon is cursed. Any burial grounds in the area are immediately overrun with the rising of the dead in the form of zombies or skeletons.\nThese undead are not under your control, but will savagely attack anyone they find.\nAnyone who dies in this cursed area immediately rises again as a zombie."
  },
  {
    "name": "Valkyrie",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "10 rounds",
    "dc": 15,
    "description": "You summon a valkyrie to your aid. [See NPC record in GM module.] She appears in a location within near and acts of her own free will to help you. She returns to Valhalla when the spell ends. You can't cast this again until you complete penance.\n\nImage: Valkyrie"
  },
  {
    "name": "Vault of the Flea",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 14,
    "description": "You bestow upon yourself and your allies within near range from you the agility of the flea, enabling them to effortlessly bound to any location within near as their movement action."
  },
  {
    "name": "Veil of Life",
    "tier": 5,
    "castingAttribute": "Constitution",
    "range": "Close",
    "duration": "1 Day",
    "dc": 15,
    "description": "With your touch, one of your undead minions appears to be alive.\nYou choose the ancestry and style of clothes worn by the undead creature. It can appear only slightly larger or smaller than the original undead minion.\nThe spell will also make the target's movements appear lifelike.\nEven the stench will be fully disguised.\nAnyone suspicious can make an INT check vs. your last spellcasting check to disbelieve the illusion."
  },
  {
    "name": "Vicious Bite",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Close",
    "duration": "Focus",
    "dc": 12,
    "description": "The teeth on an animal you touch elongates and sharpens.\nThe animal gains an extra die of damage on any bite attack.\nThis spell can be applied to Beastmasters.\n\nImage: Vicious Bite"
  },
  {
    "name": "Voice of Verity",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Far",
    "duration": "Focus",
    "dc": 12,
    "description": "Anyone who hears your chant are compelled to reveal the unvarnished truth."
  },
  {
    "name": "Void Stare",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Far",
    "duration": "Focus",
    "dc": 13,
    "description": "Your eyes turn black as you look into the dark between the stars. One creature of LV 6 or less you can see falls under your control. You decide its actions during its turn."
  },
  {
    "name": "Wall of Bones",
    "tier": 4,
    "castingAttribute": "Constitution",
    "spellType": "Damage",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 14,
    "description": "You summon a wall of writhing bones to rise from the ground.\nThe 1-foot thick wall must be contiguous and can cover a near-sized area in length and 10 feet tall or less.\nFoes close to the wall automatically take 2d4 slashing damage per round from the grasping and clawing bony hands and claws. Each close-sized section of the wall has 12 AC and 15 HP.\n\nImage: Wall of Bones",
    "damage": "2d4",
    "damageType": "slashing"
  },
  {
    "name": "Wall of Force",
    "tier": 4,
    "castingAttribute": "Intelligence",
    "range": "Near",
    "duration": "5 rounds",
    "dc": 14,
    "description": "You lift your hands, conjuring a transparent wall of force. The thin wall must be contiguous and can cover a near-sized area in width and length. You choose its shape. Nothing on the same plane can physically pass through the wall."
  },
  {
    "name": "Web",
    "tier": 2,
    "castingAttribute": "Intelligence",
    "range": "Far",
    "duration": "5 rounds",
    "dc": 12,
    "description": "You create a near-sized cube of sticky, dense spider web within the spell's range. A creature stuck in the web can't move and must succeed on a strength check vs. your spellcasting check to free itself.\n\nImage: Web",
    "opposed": 1,
    "opposedDc": 0,
    "opposedAbility": "strength"
  },
  {
    "name": "Whip of Fire Ants",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "spellType": "Damage",
    "range": "Near",
    "duration": "Instant",
    "dc": 12,
    "description": "With the snap of your fingers, a colony of fire ants form into a living needle whip that hits a creature within near, causing 1d6 slashing damage.\nThe hit causes fire ants to remain on the target, biting for an additional 1d4 piercing damage per round until they are actively removed.",
    "damage": "1d6",
    "damageType": "slashing"
  },
  {
    "name": "Whisper",
    "tier": 3,
    "castingAttribute": "Charisma",
    "range": "Touch",
    "duration": "Instant",
    "dc": 13,
    "description": "You whisper into another creature's ear, planting a false memory in its mind. You describe one brief, false memory that the target believes is true going forward. If you fail this spellcasting check, the GM chooses a short, false memory to plant in your mind, instead."
  },
  {
    "name": "Willowman",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "Instant",
    "dc": 11,
    "description": "You call upon the Willowman to appear in one creature's mind, filling it with supernatural terror. Choose one creature of LV 2 or less within range. That creature must immediately make a morale check. Even creatures that are not normally subject to morale checks (such as undead) must do so."
  },
  {
    "name": "Wish",
    "tier": 5,
    "castingAttribute": "Intelligence",
    "range": "Self",
    "duration": "Instant",
    "dc": 15,
    "description": "This mighty spell alters reality. Make a single wish, stating it as exactly as possible. Your wish occurs, as interpreted by the GM. Treat a failed spellcasting check for this spell as a critical failure, and roll the mishap with disadvantage."
  },
  {
    "name": "Witchlight",
    "tier": 1,
    "castingAttribute": "Charisma",
    "range": "Near",
    "duration": "Focus",
    "dc": 11,
    "description": "You summon a floating marsh light that bobs in the air and casts light out to a close radius around it. The light can change colors and take on vague shapes. It can float up to a near distance on your turn."
  },
  {
    "name": "Wither",
    "tier": 3,
    "castingAttribute": "Constitution",
    "range": "Near",
    "duration": "Instant",
    "dc": 13,
    "description": "You lift your hands, and all plants within near range quickly wither and die. This also dispels any spells that enhance or conjure plants.\nPlant creatures like Assassin Vines, Venus Flytraps, or Treants take 3d6 damage."
  },
  {
    "name": "Wolfshape",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Focus",
    "dc": 13,
    "description": "You and your gear transform into a wolf for the spell's duration. You assume the wolf's STR, DEX, CON, HP, AC, speed, attacks, and physical characteristics, but retain your INT, WIS, and CHA. You can cast spells in this form. If you go to 0 HP, you revert to your true shape at 0 HP. If you are level 5+, you can transform into a dire wolf or a winter wolf, instead. [See the NPC entries for wolf, dire wolf, and winter wolf in the Core Rules GM Module.]"
  },
  {
    "name": "World Serpent",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Touch",
    "duration": "Focus",
    "dc": 15,
    "description": "The torturous venom of the World Serpent drips from the weapons of a creature you touch. The target deals x2 damage with each attack (x4 on a critical hit) for the spell's duration."
  },
  {
    "name": "World Tree",
    "tier": 5,
    "castingAttribute": "Wisdom",
    "range": "Touch",
    "duration": "Focus",
    "dc": 15,
    "description": "The roots of the life-giving World Tree wrap around the soul of a creature you touch. For the spell's duration, the target can't be brought below 1 HP."
  },
  {
    "name": "Wrath",
    "tier": 4,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "10 rounds",
    "dc": 14,
    "description": "Your weapons become magical +2 and deal an additional d8 of damage dice for the spell's duration."
  },
  {
    "name": "Yesterday's Vision",
    "tier": 3,
    "castingAttribute": "Wisdom",
    "range": "Self",
    "duration": "Instant",
    "dc": 13,
    "description": "After one round of communing with your nature spirit, you can ask a single yes/no question to gain insight into an event that occurred within the last week."
  },
  {
    "name": "Zone of Truth",
    "tier": 2,
    "castingAttribute": "Wisdom",
    "range": "Near",
    "duration": "Focus",
    "dc": 12,
    "description": "You compel a creature you can see to speak truth. It can't utter a deliberate lie while within range."
  }
];

const conn = new Mongo(uri);
const db = conn.getDB(dbName);
const collection = db.getCollection(collectionName);

print(`Seeding ${collectionName} in ${dbName} at ${uri}`);
collection.deleteMany({});
collection.insertMany(spells);

print('Done.');
