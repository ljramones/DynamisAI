# DynamisAI — Ashford's Crossing
# Sim World Design Document

**Setting:** Medieval market town · Crime & Investigation · 41 characters · 7 sim days · Full arc resolution

---

## 1. World Overview

Ashford's Crossing is a prosperous market town at a river crossing, three days' ride from the capital. Old enough to have entrenched institutions, wealthy enough for two power structures to have grown up around that wealth. The town sits at the intersection of the river trade route and the King's Road.

The town has no mayor. Power is split between two institutions that have never resolved who runs what:

**THE SCALES — The Merchant Guild**
Economic power. Controls the market, docks, warehouse district, and the Crossed Keys tavern. New money. Organized, networked, transactional.

**THE CREST — The Ashford Noble House & Watch**
Political and military power. Controls the garrison, manor, mill, and law. Old blood. Hierarchical, proud, carrying secrets.

For three generations a neutral party kept the books for both: the Crown's Tax Collector, answerable only to the capital. He was the one man both factions needed and neither fully trusted.

---

## 2. The Crime

**The body of Tax Collector Emric Dole is pulled from the river on the morning the demo begins.**

The miller finds him. He was not drowned — the physic can tell. The Watch should investigate, but the Watch answers to Lord Edric Ashford, whose family's fraudulent land assessments are inside Dole's ledgers. The Guild wants those same ledgers because Dole had been quietly auditing Guild trade records at the Crown's request.

Both factions have reason to find the records. Both have reason to fear what's in them. Neither knows exactly what the other knows. Neither knows who actually did it — yet.

### The Truth — Known Only to DramaManager

Lord Edric's son Rowan confronted Dole and the confrontation turned violent. The Lord's Steward, Harro Vane, finished it without Edric's knowledge — then disposed of the body to protect the House. Four witnesses have fragments:

| Witness | What They Know | Why They Haven't Spoken |
|---------|---------------|------------------------|
| Pip (apprentice, 16) | Saw Harro Vane at the river that night | Doesn't know what it means. Tobias told him to say nothing. |
| Groom Aldric | Saw Harro leave the manor and return before dawn | Hasn't connected it to anything — total innocence. |
| Cook Marta | Knows Rowan's alibi is false | Loyal to Lady Maren; told her and no one else. |
| Ferryman Finn | Knows whose boat moved in the dark | Safety need maxed. Will not speak until safety is addressed. |

---

## 3. Locations (12)

**Market Square** `[Neutral]`
The daily hub where schedules converge. The richest rumor vector in the sim — anything said here propagates to nearly every character within two ticks.
*InfluenceMap: HIGH MOVEMENT, moderate THREAT post-murder*

**The Crossed Keys Tavern** `[Guild-controlled, mixed access]`
Brina Mast runs it and hears everything. The primary social graph mixing point. Where cross-faction relationships form and where Nessa and Tomas conduct their dangerous liaison.

**The Merchant Hall** `[Guild HQ]`
Guild meetings, record storage, Aldous Ink's archive. Access restricted to Guild members. Vespera Holt holds court here.

**The Manor House** `[Crest HQ]`
Lord Edric's seat, Lady Maren's domain, Clerk Silas Pen's office. Harro Vane controls access.
*InfluenceMap: HIGH TERRITORIAL (Crest), elevated THREAT since the murder*

**The Watch House & Garrison** `[Crest / Watch]`
Captain Wulf Dane's domain. Physically attached to the manor district but Wulf tries to maintain operational separation. Where legitimate investigation should happen — and mostly doesn't.

**The River Docks & Crossing** `[Contested]`
The crime scene. Finn the Ferryman's domain. Both factions want to control access.
*InfluenceMap: HIGH THREAT post-murder, COVER opportunities in the warehouse buildings*

**The Temple** `[Neutral]`
Brother Cael presides. Nominal sanctuary respected by both factions — the only truly safe meeting ground. Lady Maren comes here. Physic Oona works nearby.
*InfluenceMap: THREAT = 0.0 (sanctuary)*

**The Physic's House** `[Neutral]`
Oona examined Dole's body and knows it wasn't drowning. She's sitting on the most dangerous piece of knowledge in the sim.

**The Mill** `[Crest-adjacent]`
Oskar Grain's domain. He found the body and is now terrified and trying to become invisible.
*InfluenceMap: THREAT bleed from the docks*

**The Warehouse District** `[Guild-controlled]`
Curwin Rope's territory. Draven Cole uses this space for meetings that don't appear in any Guild record.

**The Workers' Quarter** `[Neutral / Low-status]`
Residential. Hilde Basket's rumor network lives here. Lower social status means characters hear from both factions without being fully trusted by either.

**The Road Out of Town** `[Contested — monitored]`
Two characters are watching it: one for the Guild, one for the Crest. Neither knows about the other. If a witness tries to leave, both factions respond.
*InfluenceMap: MOVEMENT monitoring active*

---

## 4. Social Graph Architecture

The sim has dense intra-faction edges and sparse, high-value cross-faction edges. The four witness fragments are distributed two per faction, none in the same social cluster, none trusting each other across faction lines.

| Cross-Faction Edge | Characters | System Significance |
|-------------------|-----------|-------------------|
| Romantic liaison | Nessa Coin (Scales) ↔ Corporal Tomas Fenn (Crest) | Primary cross-faction social graph edge. Both sides considering exploitation. |
| Debt loyalty | Catrin Luce (Scales) — owes Noble House | Dual-loyalty pressure. Belief divergence under faction stress. |
| Double-dealing | Mott Wheel — said yes to everyone | Multi-faction ReputationEngine crash on Day 6. |
| Secret relationship | Brother Cael — secret Guild affiliation | Destabilizes Temple neutrality if exposed. |
| Obligated informant | Tanner Bret Hide — reports to Watch | Active loyalty conflict. Cross-faction information source. |
| True neutrality | Hedgewitch Vorn — consults both factions | High social reach despite low formal status. |

**Rumor Propagation Stress Test:** Harland Beck's false conclusion (Draven did it) gets seeded into Hilde Basket on Day 1. Watch it propagate and mutate across all 41 characters over 7 days, while Pell Wynne's more targeted rumor (Rowan did it — actually correct but politically motivated) propagates through different channels. Two competing rumors, different fidelity decay curves, average fidelity 0.06 by tick 10,080.

---

## 5. Characters — The Scales (Merchant Guild, 20)

*Dense internal network. Economic power. Controls market, docks, warehouse district, Crossed Keys.*

### Leadership Tier

**01 · Alderman Vespera Holt** — Guild Master `[SCALES]`
Sharp, 60s, has survived three lords and two kings. Knows the tax audit was happening and has her own copies of the relevant records. Does not know who killed Dole but has already decided the answer must not implicate the Guild. Runs cold.
*System: DramaManager uses her to control information release pacing.*

**02 · Factor Draven Cole** — Guild Fixer `[SCALES]`
Vespera's fixer. Does what she doesn't acknowledge. Currently running two operations: find Dole's ledgers before the Watch does, and identify which Guild member Dole was feeding information to. Morally flexible, professionally competent.
*System: RumorPropagator — false rumor from Harland Beck implicates him, keeps him occupied.*

**03 · Seneschal Mira Ashby** — Guild Treasurer `[SCALES]`
The one with the most to lose financially if the audit ledgers surface. In controlled panic managing through rigid routine. Her routine is visibly wrong and other characters notice.
*System: ScheduleEngine stress test — disruption visible across multiple NPCs' perception.*

**04 · Advocate Pell Wynne** — Guild Legal Voice `[SCALES]`
Immediately began constructing a narrative implicating Rowan Ashford — partly plausible, partly useful. Spreads this through legitimate-looking channels.
*System: High-value RumorPropagator node. Competing rumor track vs. Harland Beck's false belief.*

### Merchants & Proprietors

**05 · Tobias Farr** — Cloth Merchant `[SCALES]`
Vespera's nephew. Hot-headed, loyal to faction over reason. Vespera uses him as a visible distraction while Draven does real work. Actively suppressing Pip — ordered him to say nothing.

**06 · Catrin Luce** — Spice Merchant `[SCALES]`
Owes a substantial debt to the Noble House — cross-faction loyalty pressure. Dual-loyalty character where belief divergence becomes interesting under stress.
*System: BeliefModel — dual-loyalty creates divergent belief updates from two faction sources.*

**07 · Oswin Salt** — Provisions Merchant `[SCALES]`
Genuinely neutral, old enough to remember worse times. Pressured by both sides, trying to keep his head down. Schedule: normal. Belief model: increasingly disturbed.

**08 · Brina Mast** — Innkeeper, Crossed Keys `[SCALES]`
Faction-loyal but functionally neutral by profession. Every rumor passes through her. Highest-fidelity rumor node — hears accurate information but is careful what she repeats.
*System: After Day 3, shelters Pip on Cael Mourne's instruction.*

**09 · Harland Beck** — Grain Merchant `[SCALES]`
Has independently concluded Draven Cole is the killer based on wrong circumstantial reasoning. Will spread this belief with confidence throughout Day 1.
*System: RumorPropagator — source of the primary false belief. Fidelity 0.9 at origin, decays to 0.06 by Day 7.*

**10 · Sela Torch** — Chandler, Courier Network `[SCALES]`
Runs the Guild's informal courier network. Knows who has met with whom across the whole town. Doesn't understand the significance of what she knows.

### Guild Workers & Associates

**11 · Pip Farr** — Apprentice (age 16) `[SCALES]`
Was at the river the night of the murder and saw Harro Vane. Doesn't understand what he saw. Frightened. Approaches Cael Mourne on Day 3 because Tobias said to avoid Watch and Guild — said nothing about the stranger.
*System: Need-stack — safety maxed. Beat 5 trigger — first witness testimony to reach Cael.*

**12 · Nessa Coin** — Barmaid, Crossed Keys `[SCALES]`
Romantically involved with Corporal Tomas Fenn. Genuinely fond of him. Has been leaking minor Watch operational details without thinking of it as leaking.
*System: Primary cross-faction social graph edge. Both factions considering exploitation.*

**13 · Curwin Rope** — Dockworker `[SCALES]`
Draven Cole's muscle and logistics contact. Controls who gets near the docks post-murder.
*System: SquadBlackboard — active dock-control assignment. Confrontation with Watch on Day 2 (Beat 4).*

**14 · Aldous Ink** — Guild Scribe `[SCALES]`
Has partial copies of the audit records — not the incriminating parts, but he doesn't know that. Both factions want to search his archive. He's trying to appear less significant than he is.

**15 · Mott Wheel** — Cartwright `[SCALES]`
Useful to both factions for transport. Has been saying yes to everyone and is deeply compromised. On Day 6, inadvertently confirms Harro's transport to all three factions simultaneously.
*System: ReputationEngine — multi-faction simultaneous collapse on Day 6.*

**16 · Hilde Basket** — Market Vendor `[SCALES]`
The town's dedicated gossip. Low social status, high information throughput. Propagates anything she hears immediately with personal embellishment.
*System: RumorPropagator stress test — severe fidelity decay. Harland's false belief enters the network through her.*

### Specialists

**17 · Brother Cael** — Temple Monk `[SCALES / Neutral]`
Nominally neutral. Provides Guild with quiet sanctuary — a secret that would destabilize Temple neutrality if revealed. By Day 4 holds more of the truth than anyone except Harro: Oona's medical evidence, Silas's notes, Finn's boat testimony, and the shape of Rowan's guilt.
*System: Critical neutral information accumulation node. Meeting with Cael Mourne on Day 4 Evening.*

**18 · Physic Oona** — Town Healer `[SCALES / Neutral]`
Examined Dole's body and knows he was killed before entering the water. Her schedule shows maximum disruption — she keeps walking to the Watch House and turning back. On Day 5 Harro's overreach forces her to finally go to Wulf directly.
*System: ScheduleEngine — need-conflict visible as repeated partial schedule execution. Beat 3 and Beat 9 trigger.*

**19 · Astrid Loom** — Weaver / Intelligence Asset `[SCALES]`
Guild's quiet intelligence asset in the workers' quarter. Currently tracking which Watch soldiers are spending above their wages — which points toward Piers Knot's corruption.

**20 · Finn Barge** — Ferryman `[SCALES / Neutral]`
Was awake, insomniac, watching the river the night of the murder. Knows whose boat moved and that it moved without a lantern. Has told no one. Safety need maxed, overriding everything. On Day 4 goes to the Temple.
*System: Need-stack — safety need gates all disclosure. Beat 8 trigger.*

---

## 6. Characters — The Crest (Noble House & Watch, 20)

*Hierarchical structure. Political and military power. Controls garrison, manor, mill, and law.*

### Leadership Tier

**21 · Lord Edric Ashford** — Lord of the Town `[CREST]`
Pragmatic, not cruel. Does not know Harro killed Dole. Proceeding as if Rowan is innocent, which makes his actions look like a cover-up even when they're not entirely one.
*System: His presence at Watch House for Beat 13 is Lady Maren's requirement — he must hear the confession.*

**22 · Lady Maren Ashford** — Lord's Wife `[CREST]`
Considerably sharper than Edric. Running her own investigation through Dame Isolt. On Day 5 makes the controlled demolition decision — sacrifices Harro to save Edric and Rowan.
*System: The Crest's most dangerous internal actor. Sends Dame Isolt to name Harro to Wulf on Day 5 (Beat 10).*

**23 · Captain Wulf Dane** — Watch Captain `[CREST]`
A genuinely honest man watching his investigation be obstructed by his own chain of command. Growing frustration drives his cognition rate up.
*System: DramaManager uses him as the pacing mechanism for the legitimate investigation track.*

**24 · Steward Harro Vane** — Lord's Household Manager / Killer's Facilitator `[CREST]`
The killer's facilitator. Calm, organized, experienced. Systematically eliminating evidence while appearing to assist the Watch. His schedule is the most calculated in the sim. The only character who correctly reads Cael Mourne from the start. Flees Night of Day 5. Confesses Day 7 Morning.
*System: ScheduleEngine — most calculated schedule in sim. PATTERN_BREAK anomaly on Day 6 triggers Beat 11.*

### Noble House

**25 · Rowan Ashford** — Lord's Son (age 20) `[CREST]`
Was present at the confrontation with Dole. Believes Harro saved him. Experiencing cascading guilt and terror registered as erratic affect — unusual for his baseline.
*System: AffectVector — guilt = HIGH, drops to CHRONIC after Harro's confession.*

**26 · Dame Isolt Creed** — Knight, Lady Maren's Agent `[CREST]`
Personally loyal to Lady Maren. Running Lady Maren's quiet investigation without official sanction. Delivers Harro's name to Wulf on Day 5 (Beat 10).

**27 · Reeve Bastian Thorn** — Manages Lord's Tenant Lands `[CREST]`
Had documented disputes with Dole — which makes him an obvious suspect. Being framed by implication by Harro. Doesn't fully realize it yet.

**28 · Clerk Silas Pen** — Edric's Household Scribe `[CREST]`
Knows too much about Harro's movements. Has begun hiding notes in the Temple, which Brother Cael has noticed.

### Watch Soldiers

**29 · Sergeant Alys Brand** — Wulf's Deputy `[CREST]`
Competent and honest. Field commander of the actual investigation, constantly being redirected. She and Edda Helm ride south on Day 6 Evening to catch Harro.
*System: Delivers physical evidence from Suki on Day 4 (Beat 7). Executes Harro's arrest.*

**30 · Corporal Tomas Fenn** — Watch Soldier `[CREST]`
Romantically involved with Nessa Coin. Has been leaking minor Watch operational details without thinking of it as leaking.
*System: Cross-faction social graph edge. Both factions considering instrumentalizing this relationship.*

**31 · Guard Edda Helm** — Watch Soldier, Wulf's Loyalist `[CREST]`
Independently tracking Rowan Ashford's movements outside official channels. Rides with Alys to arrest Harro on Day 6 Evening.

**32 · Guard Piers Knot** — Watch Soldier / Corrupt `[CREST]`
On Harro Vane's payroll. Redirecting patrol routes to keep the Watch away from the river at night. Weakest link in Harro's network.
*System: Executes Harro's harassment operation against Cael on Day 3 (Beat 6). Tips Harro about the warrant.*

**33 · Guard Bram Spear** — Watch Recruit (3 weeks) `[CREST]`
Doesn't understand what's happening. Assigned trivial tasks. His inexperience makes him unpredictable — he might follow procedure when everyone else deliberately doesn't.

**34 · Guard Suki Cross** — Watch Soldier `[CREST]`
Found a brass button matching Manor House livery at the river. Hasn't reported it — uncertain who to trust. On Day 4 Morning gives it to Alys Brand.
*System: DramaManager sleeper. Beat 7 trigger.*

### Servants, Tenants & Associates

**35 · Cook Marta Dell** — Manor Kitchen, Lady Maren's Loyalist `[CREST]`
Knows Rowan's alibi is false. Has told Lady Maren. Harro is keeping her inside the manor without knowing exactly what she knows.

**36 · Groom Aldric Hoof** — Manor Stables `[CREST]`
Saw Harro leave the manor grounds late the night of the murder and return before dawn. Has connected this to nothing — total innocence. On Day 5 Evening delivers the final confirmation to Cael with complete innocence.
*System: Final piece of Cael's truth assembly. Delivered without pressure.*

**37 · Miller Oskar Grain** — Mill Operator `[CREST]`
Found the body. Told the Watch immediately. Now trying to become invisible — not eating, not sleeping normally, not leaving the mill.
*System: ScheduleEngine — maximum need-stack disruption.*

**38 · Tenant Ffion Dirt** — Farmer `[CREST]`
Her land assessment was the document Rowan confronted Dole about. At her farmstead outside town, unaware she's being looked for. Both factions searching for her for opposite reasons.

**39 · Tanner Bret Hide** — Craftsman / Obligated Informant `[CREST]`
Reluctant Watch informant — not by choice but by obligation. Reports to Wulf on Guild movements while hating himself for it.
*System: Active loyalty conflict. Cross-faction information source.*

**40 · Hedgewitch Vorn** — Independent Practitioner `[NEUTRAL]`
Lives just outside town. Consulted by both factions. Her neutrality is genuine. High social reach despite low formal status.

---

## 7. Character 41 — Cael Mourne (The Stranger)

**41 · Cael Mourne** — The Stranger / Former Crown Investigator `[NEUTRAL]`

Arrives on foot, which is unusual. Carries a sword he clearly knows how to use and asks no questions about the murder before Lord Edric does — meaning he already knew about it. Takes Edric's commission without negotiating the price.

A former Crown investigator, dismissed two years ago for finding politically inconvenient truth. He came to Ashford's Crossing because the Crown sent no one official to investigate the murder of their own tax collector — and that silence is itself a message. Edric's commission is convenient cover.

He knows both factions are bad. The cynicism cracks on Day 5 Evening when Groom Aldric, answering a casual question about horse schedules, delivers the final confirmation with total innocence.

**Key relationships:**
- **Lord Edric:** Employer, not ally. Both men know the commission is dishonest.
- **Lady Maren:** The one person he genuinely respects after their first conversation.
- **Captain Wulf Dane:** Professional respect, immediate. They leave information where the other will find it.
- **Harro Vane:** The only character who correctly reads Cael from the start. Their awareness of each other drives the final act.
- **Draven Cole:** Attempts to use Cael, discovers Cael is using him back. Wary mutual respect.
- **Pip Farr:** The witness who comes first. Cael protects him deliberately — tavern cover role, Brina's supervision.

**System demonstrations:**
- Social graph cold start: zero edges at tick 0, full model by tick 10,080
- Dual SquadBlackboard membership (Crest + Guild) with zero objective compliance
- Deliberate asymmetric RumorPropagation — one source, two graphs, controlled information
- Fastest BeliefModel convergence in the sim — high-perception baseline, no obstruction
- Need-stack-driven witnesses: characters approach him based on their own need calculations
- ReputationEngine cold start: both factions' opinion diverges from the same blank starting point

---

## 8. Tick Architecture

| Parameter | Value | Notes |
|-----------|-------|-------|
| Sim day duration | 1,440 ticks | 1 tick = 1 sim minute |
| Full run | 10,080 ticks | 7 days |
| Real time ratio | ~1.25 seconds / tick | Full run ≈ 3.5 hours |
| Sim plane rate | Every tick | Never blocks, never calls LLM |
| Cognition plane rate | Every 60 ticks | ~1 sim hour deliberative rate |
| DramaManager eval window | Every 120 ticks | 84 evaluation windows total |
| LLM gate (BudgetGovernor) | ~60 ticks | ~1 Hz deliberative rate per NPC |

**Period definitions:**

| Period | Tick Range | Sim Time |
|--------|-----------|---------|
| Dawn | 0–239 | Midnight to 4am |
| Morning | 240–599 | 4am to 10am |
| Afternoon | 600–959 | 10am to 4pm |
| Evening | 960–1199 | 4pm to 8pm |
| Night | 1200–1439 | 8pm to midnight |

---

## 9. DramaManager Beat Structure — 7 Days

DramaManager tracks five engagement metrics: **tension**, **information density**, **faction pressure**, **witness safety**, and **investigation momentum**.

### Day 0 — Pre-Sim State

Dole's body enters the river at tick -180. Oskar Grain finds it at tick 0. Sim opens mid-morning Day 1 with the body already found and Watch notified.

**Starting WorldState flags:**
```
dole.alive = false
dole.location = river_mill
harro.cleanup_in_progress = true
rowan.affect.guilt = HIGH
oona.knowledge.cause_of_death = KNOWN
oona.disclosure = NONE
cael.in_town = false
cael.belief.complete = false
harro.fled = false
harro.in_custody = false
harro.confessed = false
```

---

### Beat 1 — LORD_COMMISSION · Day 1
**Trigger:** Cael has been in town 120 ticks without approaching either faction
**Hold:** Fires only during Afternoon period with Edric's schedule at Manor Hall
**System:** SquadBlackboard
**Event:** Edric summons Cael formally. Both men know the commission is dishonest. `squad.crest.investigator = cael`

### Beat 2 — GUILD_APPROACH · Day 1
**Trigger:** Guild faction receives RumorEvent that Cael took the Crest commission
**Hold:** Requires Draven Cole's schedule to be unoccupied
**System:** SquadBlackboard dual membership
**Event:** Draven offers double Edric's fee. Cael accepts. `squad.guild.asset = cael`. He now has two entries and zero intention of serving either.

*Day 1 Evening: Harland Beck tells Hilde Basket that Draven Cole killed Dole. By tick 1,200 this false belief has reached 11 characters with fidelity ranging from 0.9 (direct) to 0.4 (third-generation). The 7-day RumorPropagator stress test begins.*

---

### Beat 3 — OONA_PARTIAL_DISCLOSURE · Day 2
**Trigger:** Information density below threshold AND tick > 1,600
**Hold:** Fires only if `oona.schedule.disruption >= 3` (she has walked to Watch House and turned back at least three times)
**System:** ScheduleEngine disruption / need-conflict
**Event:** Oona tells Brother Cael the monk, not Wulf. Medical evidence enters a neutral node. Monk also holds Silas's hidden notes.

### Beat 4 — DOCK_CONFRONTATION · Day 2
**Trigger:** Faction pressure crosses first threshold — both factions deploy assets at docks simultaneously
**System:** SquadBlackboard HOSTILE_CONTACT / InfluenceMap
**Event:** Curwin (Guild) and Watch patrol standoff. Wulf de-escalates. THREAT layer at docks spikes.

---

### Beat 5 — PIP_APPROACHES · Day 3
**Trigger:** `pip.safety_need > threshold` AND `tobias.schedule.location ≠ CROSSED_KEYS` AND `cael.schedule.location = CROSSED_KEYS`
**Hold:** Cael must have been in town at least 2 days
**System:** BeliefModel cold-start update
**Event:** First fragment of direct witness testimony. Pip describes Harro without knowing the name. `cael.belief.suspect_profile = SENIOR_MANOR_FIGURE`. Pip placed in tavern cover role immediately.

### Beat 6 — HARRO_ESCALATES · Day 3
**Trigger:** `cael.belief.suspect_profile = SENIOR_MANOR_FIGURE` detected AND `harro.threat_assessment.cael = HIGH`
**System:** ReputationEngine / SquadBlackboard
**Event:** Piers Knot manufactures false theft accusation against Cael. Wulf dismisses it within 40 ticks. But Cael now knows someone in the Manor is watching him. `cael.belief.opponent_aware = true`.

---

### Beat 7 — PHYSICAL_EVIDENCE · Day 4
**Trigger:** `suki.evidence.river_button = DELIVERED_TO_ALYS`
**System:** BeliefModel convergence / physical evidence
**Event:** Brass button matching Manor livery found at origin point. Alys brings it to Wulf. Wulf begins formal request for manor livery records. Harro intercepts. `harro.threat_assessment.watch = CRITICAL`.

### Beat 8 — FINN_TESTIMONY · Day 4
**Trigger:** `finn.safety_need` below flight threshold AND `cael.reputation.neutral = HIGH` AND tick > 4,500
**Hold:** Finn must not be in line-of-sight of any faction member — walks to Temple
**System:** RumorPropagator neutral node
**Event:** Finn tells Brother Cael the monk about the boat — manor-side, no lantern, returned before dawn. Monk now holds: medical evidence, Silas's notes, Rowan's guilt profile, and boat testimony. Day 4 Evening: monk summons Cael Mourne to the Temple.

---

### Beat 9 — HARRO_OVERREACH · Day 5
**Trigger:** Cael deliberately injects asymmetric false rumor to Draven (Harro blackmail threat) AND true rumor to Harro via Piers (Oona going to Wulf)
**System:** RumorPropagator asymmetric injection
**Event:** Harro sends servant to intercept Oona. Oona reads it as a threat and goes directly to Wulf instead — the first time she completes the walk. `oona.disclosure = WULF_DANE`. Wulf now has physical evidence, medical evidence, and a formal witness statement.

### Beat 10 — HARRO_NAMED · Day 5
**Trigger:** `dame_isolt.message = DELIVERED_TO_WULF`
**System:** ReputationEngine cascade / multi-faction
**Event:** Lady Maren's controlled demolition. Dame Isolt gives Wulf the name. Warrant issued. Alys and Edda assigned. Wulf goes to garrison, not manor. Harro learns via Piers within 20 ticks and begins moving.

*Day 5 Evening: Cael goes to the stables. Aldric, answering a casual question about horse schedules, mentions Harro leaving in the small hours. Aldric is already on the next topic before Cael's expression changes. `cael.belief_model.complete = TRUE`.*

---

### Beat 11 — FLIGHT_DETECTED · Day 6
**Trigger:** ScheduleEngine registers PATTERN_BREAK anomaly for Harro — room empty, schedule not executing
**System:** ScheduleEngine PATTERN_BREAK / InfluenceMap / multi-faction
**Event:** Tension metric spikes. Wulf deploys to road checkpoints. Three factions now monitoring the same exit point, none coordinating. Mott Wheel confirms transport to all three factions within 90 ticks. Multi-faction ReputationEngine crash.

### Beat 12 — CAEL_COMMITS · Day 6
**Trigger:** `cael.belief_model.complete = TRUE` AND `harro.location = UNKNOWN` AND tick > 7,500
**System:** DramaManager arc resolution
**Event:** Cael goes to Wulf Dane. Lays out the complete truth: Rowan's confrontation, Harro's independent action, the full witness chain, spatial analysis. Gives Wulf where Harro will have gone. Does not collect Vespera's retainer. Leaves.

---

### Beat 13 — HARRO_CONFESSES · Day 7
**Trigger:** `harro.in_custody = TRUE` AND `wulf.evidence.complete = TRUE`
**Hold:** Fires only during Morning period, requires Lord Edric present at Watch House
**System:** AffectVector resolution
**Event:** Edric hears it. Harro tells the full truth. Rowan is cleared of the killing, not of the confrontation. Edric does not speak. `rowan.affect.guilt = ACUTE → CHRONIC`.

### Beat 14 — EQUILIBRIUM_RESTORED · Day 7
**Trigger:** `harro.confession = TRUE` AND faction pressure below threshold
**System:** ScheduleEngine mass reset / InfluenceMap normalization
**Event:** THREAT layer drops across all locations. ScheduleEngine baselines begin resetting. Cross-faction social graph edges that went cold reactivate. Pip returns to normal apprentice routine by Day 7 Evening.

---

## 10. Beat Summary Table

| Beat | Name | Day | Trigger Type | System Exercised |
|------|------|-----|-------------|-----------------|
| 1 | LORD_COMMISSION | 1 | Schedule + timer | SquadBlackboard |
| 2 | GUILD_APPROACH | 1 | RumorEvent cascade | SquadBlackboard dual membership |
| 3 | OONA_PARTIAL_DISCLOSURE | 2 | Need-stack conflict | ScheduleEngine disruption |
| 4 | DOCK_CONFRONTATION | 2 | InfluenceMap threshold | SquadBlackboard HOSTILE_CONTACT |
| 5 | PIP_APPROACHES | 3 | Need-stack + schedule gap | BeliefModel cold-start update |
| 6 | HARRO_ESCALATES | 3 | Belief state detection | ReputationEngine + SquadBlackboard |
| 7 | PHYSICAL_EVIDENCE | 4 | Character action completion | BeliefModel convergence |
| 8 | FINN_TESTIMONY | 4 | Need-stack threshold | RumorPropagator neutral node |
| 9 | HARRO_OVERREACH | 5 | Deliberate false rumor | RumorPropagator asymmetric injection |
| 10 | HARRO_NAMED | 5 | Cross-faction cooperation | ReputationEngine cascade |
| 11 | FLIGHT_DETECTED | 6 | ScheduleEngine PATTERN_BREAK | InfluenceMap + multi-faction |
| 12 | CAEL_COMMITS | 6 | BeliefModel complete flag | DramaManager arc resolution |
| 13 | HARRO_CONFESSES | 7 | Custody + evidence complete | AffectVector resolution |
| 14 | EQUILIBRIUM_RESTORED | 7 | Faction pressure drop | ScheduleEngine mass reset |

---

## 11. Day 7 Final State

| Character | Outcome |
|-----------|---------|
| Harro Vane | In Watch custody. Confessed fully. Asked only that Rowan be considered separately. |
| Rowan Ashford | Cleared of killing, not of confrontation. `rowan.affect.guilt = ACUTE → CHRONIC`. |
| Lord Edric | Diminished in authority, not in title. Crown investigation ongoing. |
| Lady Maren | Effectively in charge of what the House becomes next. Controlled demolition worked. |
| Captain Wulf Dane | The only institutional actor who performed his function correctly. |
| Vespera Holt | Survived. Cooperated first, destroyed Mira Ashby's career in the process. |
| Mira Ashby | Destroyed. Took formal culpability for Guild's fraudulent records. |
| Cael Mourne | Two hours south on the King's Road. Collected Edric's fee only. Doesn't know Harro confessed — and was right anyway. |
| Harland Beck's false rumor | Fidelity 0.06 by tick 10,080. Still present in three characters' belief models but below actionable threshold. |