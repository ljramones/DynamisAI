# DynamisAI — Ashford's Crossing
# ScheduleEngine Character Schedules

**Tick reference:** 1 tick = 1 sim minute · 1440 ticks = 1 day · 10,080 ticks = full 7-day run

**Period definitions:**
- Dawn 0–239 · Morning 240–599 · Afternoon 600–959 · Evening 960–1199 · Night 1200–1439

**Need stack:** Safety · Sustenance · Rest · Social · Purpose (priority order, high = urgent)

---

## TIER 1 — Full 7-Day Schedules
*Tick ranges · location · activity · need-stack state · beat interactions*

---

### HARRO VANE — Steward, Manor House
*The most calculated schedule in the sim. Every action has a cover. PATTERN_BREAK on Day 6 Night is the clearest anomaly signal in the entire run.*

**Baseline personality:** Routine-first. Arrives at every location exactly on time. Never lingers. Never deviates. Other characters use his schedule as a clock.

---

#### Day 1

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–59 | Dawn | Manor — private quarters | Sleep | Rest: satisfied | Has been awake most of the night. This is recovery sleep. |
| 60–239 | Dawn | Manor — private quarters | Sleep | Rest: recovering | |
| 240–299 | Morning | Manor — kitchen passage | Morning routine, cold breakfast | Sustenance: satisfied | Exactly on time. Cook Marta observes him. Affect: controlled. |
| 300–479 | Morning | Manor — Edric's study | Daily briefing with Lord Edric | Purpose: active | Provides cover narrative for Rowan. Edric half-believes it. |
| 480–599 | Morning | Manor — household rounds | Inspect servants' work, assign tasks | Purpose: active | Assigns Silas Pen clerical work that keeps him occupied. |
| 600–719 | Afternoon | Manor — gate | Receives news of Cael Mourne's arrival | Safety: rising | Sela Torch's courier delivers the report. Harro does not react visibly. |
| 720–839 | Afternoon | Manor — records room | Reviews Dole's official correspondence | Purpose: active | Removing anything that creates a timeline problem. Looks like administrative work. |
| 840–959 | Afternoon | Watch House | Courtesy visit to Wulf Dane | Purpose: active | Offers the Watch full manor cooperation. Surfaces Bastian Thorn's name as a person of interest. Plants the false trail. |
| 960–1079 | Evening | Manor — hall | Supper with household | Sustenance: satisfied | Visible, relaxed, normal. |
| 1080–1199 | Evening | Manor — study | Reviews day, writes in household log | Purpose: active | Log entries are carefully constructed. |
| 1200–1439 | Night | Manor — private quarters | Sleep | Rest: active | |

**Day 1 beat interaction:** None directly. Plants Bastian Thorn trail during Watch House visit. Receives Cael intel via Sela.

---

#### Day 2

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–239 | Dawn | Manor — private quarters | Sleep | Rest: satisfied | |
| 240–359 | Morning | Manor — kitchen passage | Morning routine | Sustenance: satisfied | |
| 360–479 | Morning | Manor — Edric's study | Briefing: Oona's examination findings | Safety: elevated | Edric asks about the physic. Harro says he'll look into it. |
| 480–599 | Morning | Dock district — brief visit | Checks that the origin point is clear | Safety: active | Travels without livery. Returns before noon. |
| 600–719 | Afternoon | Watch House | Second courtesy visit | Purpose: active | Redirects Wulf toward the Bastian Thorn angle. Brings a household record that looks helpful and isn't. |
| 720–839 | Afternoon | Manor — household | Routine management | Purpose: active | |
| 840–959 | Afternoon | Manor — private meeting | Meets with Piers Knot | Safety: active | Assigns Piers new patrol route instructions. Payment made. |
| 960–1079 | Evening | Manor — hall | Supper | Sustenance: satisfied | |
| 1080–1199 | Evening | Manor — Rowan's quarters | Private visit to Rowan | Safety: active | Checks that Rowan is holding. Rowan's affect is wrong. Harro notices. |
| 1200–1439 | Night | Manor — private quarters | Sleep, interrupted | Rest: degraded | Wakes twice. Runs contingency thinking. |

**Day 2 beat interaction:** Beat 4 (dock confrontation) — Harro was in the dock district during Morning. He does not intervene in the standoff. He is already gone when it occurs.

---

#### Day 3

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–239 | Dawn | Manor — private quarters | Sleep | Rest: recovering | |
| 240–359 | Morning | Manor — kitchen | Morning routine | Sustenance: satisfied | |
| 360–479 | Morning | Manor — records | Reviews Watch House request re: Bastian Thorn | Purpose: active | Satisfies the request selectively. |
| 480–599 | Morning | Manor — external grounds | Walks the grounds | Safety: elevated | Thinks. The Cael Mourne situation is not resolving. |
| 600–719 | Afternoon | Manor — gate | Receives intelligence: Cael is asking about manor-side figures | Safety: HIGH | Sela Torch's network delivers this. |
| 720–839 | Afternoon | Manor — private | Instructs Piers Knot to execute harassment operation | Safety: active | The false theft accusation against Cael. Harro calculates the risk as low. He is wrong about this. |
| 840–959 | Afternoon | Manor — Edric's study | Normal briefing | Purpose: active | Perfectly normal. Edric notices nothing. |
| 960–1079 | Evening | Manor — hall | Supper | Sustenance: satisfied | |
| 1080–1199 | Evening | Manor — private quarters | Reviews contingency plan | Safety: rising | First time he commits the contingency to mental preparation rather than just planning. |
| 1200–1439 | Night | Manor — private quarters | Sleep, poor | Rest: degraded | |

**Day 3 beat interaction:** Beat 6 (Harro escalates) fires during Afternoon. Piers Knot executes during Evening period. Wulf dismisses it within 60 ticks. Cael's belief `opponent.aware = TRUE` updates.

---

#### Day 4

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–239 | Dawn | Manor — private quarters | Sleep | Rest: poor | |
| 240–359 | Morning | Manor — kitchen | Morning routine | Sustenance: satisfied | |
| 360–479 | Morning | Manor — gate records | Intercepts Wulf's livery records request before it reaches Silas | Safety: HIGH | This is the fastest he has moved in four days. Silas does not see the request. |
| 480–599 | Morning | Manor — records room | Prepares alternative livery documentation | Safety: active | The button evidence is unknown to him at this point. |
| 600–719 | Afternoon | Watch House | Third courtesy visit | Purpose: active | Wulf is visibly colder. Harro notes this. `harro.threat_assessment.watch = CRITICAL` |
| 720–839 | Afternoon | Manor — Rowan's quarters | Extended visit to Rowan | Safety: HIGH | Rowan's affect is getting worse. Harro tells him it will be over soon. |
| 840–959 | Afternoon | Manor — study | Writes contingency logistics | Safety: active | Route, timing, transport. All committed to memory, nothing written down. |
| 960–1079 | Evening | Manor — hall | Supper | Sustenance: satisfied | Surface calm. Dame Isolt watches him across the table. |
| 1080–1199 | Evening | Manor — private quarters | Final logistics review | Safety: active | |
| 1200–1439 | Night | Manor — private quarters | Light sleep | Rest: minimal | |

**Day 4 beat interaction:** Beat 7 (physical evidence) — Harro does not know about the button. Beat 8 (Finn speaks) — he does not know Finn talked. His information gap is widening.

---

#### Day 5

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–239 | Dawn | Manor — private quarters | Awake before dawn | Rest: abandoned | |
| 240–359 | Morning | Manor — kitchen | Minimal breakfast | Sustenance: minimal | First visible schedule deviation — he is usually the last to leave the kitchen. Today he leaves first. Cook Marta notices. |
| 360–479 | Morning | Manor — Edric's study | Briefing | Purpose: active | Normal content. He does not mention Cael Mourne. |
| 480–599 | Morning | Manor — via Piers Knot | Receives Cael's false intelligence (Oona going to Wulf) | Safety: CRITICAL | Piers delivers this. Harro makes the decision to intercept Oona within 20 ticks. |
| 600–659 | Afternoon | Manor — sends servant to Oona's house | Dispatches servant with "invitation" to discuss findings privately | Safety: active | This is his first reactive move. Every previous action was managed. This one is not. |
| 660–779 | Afternoon | Manor — private quarters | Waits | Safety: CRITICAL | The wait is the most dangerous part of his day. He is not in control of what happens next. |
| 780–839 | Afternoon | Manor — Edric's study | Receives report: Oona went to Watch House instead | Safety: CRITICAL | `oona.disclosure = WULF_DANE` confirmed via manor staff intelligence within 20 ticks. |
| 840–959 | Afternoon | Manor — private quarters | Activates contingency | Safety: overriding all other needs | Mott Wheel contacted via intermediary. Horse arranged for the following night. |
| 960–1079 | Evening | Manor — hall | Supper. Final supper. | All needs suppressed by Safety | Surface completely normal. This is his best performance of the entire sim. |
| 1080–1199 | Evening | Manor — private quarters | Packs what matters. Nothing that would be missed before morning. | Safety: active | |
| 1200–1439 | Night | Manor → road | Departs. Alone. On foot to the arranged horse. No lantern. | Safety: driving everything | PATTERN_BREAK will register at tick 1440 when his Dawn routine does not execute. |

**Day 5 beat interactions:** Beat 9 (Harro overreach) fires at tick ~480–600. Beat 10 (Harro named) — Dame Isolt's message to Wulf fires during Afternoon while Harro is already in contingency mode. He learns of the warrant via Piers at tick ~900. He was already leaving.

---

#### Day 6

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–1439 | All | Road south — secondary route | Traveling. Alone. Slowly. | Safety: the only active need | ScheduleEngine: no entries execute. PATTERN_BREAK registers at tick 1440. He is two hours from the next town when Alys and Edda catch him at approximately tick 9400. |

**Day 6 beat interaction:** Beat 11 (flight detected) fires when PATTERN_BREAK anomaly registers. He offered no resistance when found. He was traveling slowly. He had already decided.

---

#### Day 7

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–480 | Dawn/Morning | Watch House — holding | In custody | All needs subordinate | Alys and Edda returned overnight. |
| 480–600 | Morning | Watch House — room | Confession | Purpose: final | Lord Edric present at Lady Maren's instruction. Harro tells the full truth. Asks only that Rowan be considered separately. Does not ask for mercy. |
| 600–1439 | Afternoon–Night | Watch House — cell | Awaiting Crown disposition | Rest, Sustenance: basic | Schedule engine: minimal entries. The arc is complete. |

---

### CAEL MOURNE — The Stranger
*Zero social graph edges at tick 0. Most irregular schedule in the sim. No fixed routine — which itself registers as a perceptual anomaly to Harro and other high-perception NPCs.*

**Baseline personality:** No baseline. Every day is built from what the previous day revealed. He goes where the investigation requires. He eats when he remembers. He sleeps when it is safe.

---

#### Day 1

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 0–239 | Dawn | [in transit — on the King's Road] | Walking. Already knew about the murder. | Purpose: active | Arrives during Morning. No prior arrangements. |
| 240–299 | Morning | Town gate → Crossed Keys | Arrives on foot. Takes a room. Asks nothing about the murder. | Sustenance: low, Safety: assessing | Brina notes the silence. Sela Torch's network flags him within 20 ticks. |
| 300–479 | Morning | Crossed Keys — common room | Eats. Watches. Maps the room's social topology. | Sustenance: satisfying, Purpose: active | He learns more from watching Brina manage her customers for two hours than most people learn in a week. |
| 480–599 | Morning | Market Square | Walks. No apparent destination. Watches routines. | Purpose: active | Notes Oskar Grain's absence from his usual stall. First anomaly logged. |
| 600–719 | Afternoon | Manor — Edric's hall | Lord's commission. Takes it without negotiating. | Purpose: active | Beat 1. `squad.crest.investigator = cael` |
| 720–839 | Afternoon | Market Square → docks perimeter | Walks the geography. Does not approach the crime scene directly. | Purpose: active | Reading the spatial layout. Notes the river's flow direction. |
| 840–959 | Afternoon | Crossed Keys | Present. Visible. Available. | Social: passive | Harro's intelligence reaches him here: Cael logged as "unknown, armed, commission from Edric." |
| 960–1079 | Evening | Crossed Keys | Draven Cole's approach. Accepts second retainer without hesitation. | Purpose: active | Beat 2. `squad.guild.asset = cael`. Both entries now active. |
| 1080–1199 | Evening | Crossed Keys — room | Reviews the day. | Purpose: active | Notes: Oskar absent. Rowan's affect wrong at manor. Harro perfectly calibrated — too calibrated. |
| 1200–1439 | Night | Crossed Keys — room | Sleep. Light. | Rest: partial | Does not sleep deeply in unfamiliar places. Never has. |

---

#### Day 2

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–399 | Morning | Crossed Keys | Breakfast. Talks to Brina — not about the murder, about the town's rhythms. | Sustenance: satisfied, Social: light | She tells him more than she intends to. |
| 400–599 | Morning | Workers' Quarter | Walks. Observes Hilde Basket's operation. | Purpose: active | Notes information propagation pattern. Will use this. |
| 600–719 | Afternoon | Mill | First deliberate contact. Approaches Oskar Grain. | Purpose: active | Does not ask about the body. Asks about the mill, the river, what Oskar normally sees at dawn. Oskar is terrified. Cael notes the depth of the fear — this is not ordinary grief. |
| 720–839 | Afternoon | Dock perimeter | Watches the Curwin/Watch standoff from distance. | Safety: assessing | Beat 4 fires here. He does not intervene. He is learning faction behavior patterns. |
| 840–959 | Afternoon | River bank — upstream from mill | Geography work. Alone. | Purpose: active | Establishes the origin point constraint. `cael.belief.origin_point = MANOR_SIDE_UPSTREAM` |
| 960–1079 | Evening | Crossed Keys | Present. Visible. | Social: passive | Delivers a minor piece of Guild intelligence to Draven — true, harmless, establishing credibility. |
| 1080–1199 | Evening | Crossed Keys — room | Reviews. | Purpose: active | |
| 1200–1439 | Night | River — walks the bank alone | Reads the geography at night, as it would have been. | Purpose: active | The most important two hours of Day 2. Confirms the origin point. Notes what's visible from the mill loft at night. |

---

#### Day 3

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–399 | Morning | Crossed Keys | Late rise. Eats. | Rest: recovering, Sustenance: satisfied | |
| 400–599 | Morning | Market Square — perimeter | Watches manor-side figures moving through the morning market. Elimination process. | Purpose: active | He is identifying Harro Vane by observing who doesn't fit the social patterns their role should produce. |
| 600–719 | Afternoon | Crossed Keys — common room | Present during the afternoon lull. | Purpose: passive | Beat 5 fires here. Pip approaches. |
| 600–660 | Afternoon | Crossed Keys — corner table | Pip's testimony. Receives the first witness fragment. | Purpose: HIGH | `cael.belief.suspect_profile = SENIOR_MANOR_FIGURE`. Does not react visibly. |
| 661–719 | Afternoon | Crossed Keys | Arranges Pip's cover role with Brina in under 10 minutes. | Purpose: active | Three ticks of deliberate social engineering. Brina asks no questions. |
| 720–839 | Afternoon | Watch House — exterior | Does not enter. Watches who comes and goes. | Purpose: active | Notes Alys Brand's body language when she exits — fury, not confusion. The investigation is being obstructed from inside. |
| 840–899 | Afternoon | Market Square | Piers Knot's harassment operation begins. False theft accusation. | Safety: minor irritation | Beat 6. He handles it with Wulf within 40 ticks. More importantly: he now knows someone in the manor is watching him closely. `cael.belief.opponent.aware = TRUE` |
| 900–959 | Afternoon | Crossed Keys | Returns. Accelerates internal timeline. | Purpose: HIGH | |
| 960–1079 | Evening | Crossed Keys | Delivers false intelligence to Draven: Harro planning to blackmail Vespera directly. | Purpose: active | First deliberate asymmetric injection. Guild redirected. |
| 1080–1199 | Evening | Crossed Keys — room | Prepares Day 4 approach. | Purpose: active | |
| 1200–1439 | Night | Crossed Keys — room | Sleep. Better than the previous two nights. | Rest: partial | |

---

#### Day 4

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–399 | Morning | Manor gate — exterior | Watches the morning movement. Elimination continues. | Purpose: active | Identifies Harro Vane by process of observation. `cael.belief.suspect = HARRO_VANE` No interrogation. No confrontation. Just watching who moves how. |
| 400–479 | Morning | Market Square | Appears idle. | Purpose: active | |
| 480–599 | Morning | Crossed Keys | Breakfast, late. | Sustenance: satisfied | |
| 600–779 | Afternoon | Crossed Keys — common room | Present. Visible. Available. | Social: passive | He is now making himself legible as genuinely outside the factions. Finn's safety calculation is shifting. |
| 780–959 | Afternoon | Workers' Quarter | Walks. Visible in Finn's territory without approaching him. | Purpose: active | Patience. |
| 960–1079 | Evening | Temple — exterior | Waits for Brother Cael's summons. | Purpose: active | He knew it was coming. Two neutral parties with complementary information — the logic was inevitable. |
| 1080–1199 | Evening | Temple — interior | Meeting with Brother Cael the monk. | Purpose: HIGH | Combined truth assembly. Receives: medical evidence (Oona), Silas's notes, Finn's boat testimony, Rowan's guilt profile. Still missing: direct confirmation that the manor figure was Harro specifically. |
| 1200–1439 | Night | Crossed Keys — room | Processes. | Purpose: active | `cael.belief_model` nearly complete. One piece missing. He knows what it is and who has it. |

---

#### Day 5

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–359 | Morning | Crossed Keys | Breakfast. | Sustenance: satisfied | |
| 360–479 | Morning | Market Square → Guild district | Delivers second asymmetric injection to Draven: Harro blackmail threat. | Purpose: active | Draven redirected. Dock access loosens. |
| 480–539 | Morning | Via Piers Knot | Delivers true information to Harro: Oona is going to Wulf. | Purpose: active | Beat 9 set-up. He knows this will force Harro to move. The overreach is engineered. |
| 540–599 | Morning | Returns to Crossed Keys | | Purpose: active | |
| 600–779 | Afternoon | Crossed Keys — room | Waits. | Purpose: HIGH | He has set the board. Now he watches. |
| 780–839 | Afternoon | Market Square | Observes Oona walking to Watch House — completing the walk for the first time. | Purpose: HIGH | Beat 9 fires. He watches her go in. He does not follow. |
| 840–959 | Afternoon | Manor — stables | Casual visit. Asks Aldric about horse routines. | Purpose: active | Appears social. He is running the final confirmation. |
| 900–920 | Afternoon | Manor — stables | Aldric answers a casual question about horse schedules. | Purpose: CRITICAL | `cael.belief_model.complete = TRUE`. Aldric is already talking about something else. Cael's expression does not change. |
| 920–959 | Afternoon | Departs manor | | Purpose: resolved | |
| 960–1079 | Evening | Crossed Keys — room | Sits with the complete truth. | Purpose: resolved, all others quiet | The cynicism crack. The full shape of it. Rowan is twenty. Harro acted alone. The system made this inevitable. |
| 1080–1199 | Evening | Crossed Keys | One drink. Does not speak to anyone. | Social: closed | |
| 1200–1439 | Night | Crossed Keys — room | Sleep. The best sleep of the run. | Rest: full | He knows what he's going to do. |

---

#### Day 6

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–359 | Morning | Watch House | Goes to Wulf Dane. | Purpose: final | Beat 12. Lays out everything. Gives Wulf the route Harro will have taken. Documented. Named. Unburiable. |
| 360–479 | Morning | Manor | Collects Edric's commission fee. | Purpose: complete | |
| 480–539 | Morning | Manor — corridor | Passes Lady Maren. She thanks him. He says nothing. | Social: closed | |
| 540–599 | Morning | Town gate | Leaves. | All needs satisfied or irrelevant | He does not know Harro confessed. He assumed he would. He was right. He will never know he was right. |
| 600–1439 | Afternoon–Night | King's Road south | Walking. | Purpose: drifting | Not in the sim anymore. |

#### Days 6–7 (Cael): Not present. Schedule engine: no entries.

---

### OONA — Physic
*Need-conflict between truth-telling and safety visible as repeated partial schedule execution. The ScheduleEngine stress test for interrupted routines.*

**Baseline:** Methodical, starts early, finishes late. Trusted by both factions as a healer. Her house is between the Temple and the market — she sees everyone who passes.

---

#### Day 1

| Ticks | Period | Location | Activity | Need State |
|-------|--------|----------|----------|------------|
| 0–59 | Dawn | Physic's house | Awake early — examined the body last night, cannot sleep | Rest: abandoned |
| 60–239 | Dawn | Physic's house | Records examination findings in private notes. Cause of death: blunt trauma prior to drowning. | Purpose: active |
| 240–479 | Morning | Physic's house / rounds | Morning patients. Routine. | Sustenance, Purpose: satisfied |
| 480–599 | Morning | Market Square | Routine errands. Passes Watch House. Does not stop. First incomplete execution. | Safety: rising |
| 600–839 | Afternoon | Physic's house | Afternoon patients | Purpose: active |
| 840–899 | Afternoon | Walks toward Watch House | Gets to within 30 meters. Turns back. Second incomplete execution. | Safety: HIGH |
| 900–959 | Afternoon | Physic's house | Returns. Resumes work. Visibly agitated. | Safety: active |
| 960–1199 | Evening | Temple | Evening prayer. Longer than usual. | Social: seeking comfort |
| 1200–1439 | Night | Physic's house | Poor sleep | Rest: degraded |

---

#### Day 2

| Ticks | Period | Location | Activity | Need State |
|-------|--------|----------|----------|------------|
| 240–479 | Morning | Physic's house / rounds | Patients. Routine. | Purpose: satisfied |
| 480–599 | Morning | Walks toward Watch House | Reaches the door. Does not knock. Third incomplete execution. | Safety: CRITICAL |
| 600–839 | Afternoon | Physic's house | Patients | Purpose: active |
| 840–959 | Afternoon | Temple | Finds Brother Cael. Tells him everything. | Purpose: released, Safety: partially resolved |

**Beat 3 fires at tick ~840.** `oona.schedule.disruption = 3` threshold met. She tells Brother Cael, not Wulf. The information enters a neutral node.

| 960–1199 | Evening | Physic's house | Returns. Slightly less agitated. She has told someone. | Safety: partial |
| 1200–1439 | Night | Physic's house | Better sleep. Not good. | Rest: partial |

---

#### Day 3

| Ticks | Period | Location | Activity | Need State |
|-------|--------|----------|----------|------------|
| 240–599 | Morning | Physic's house / rounds | Patients. Routine. Mostly normal. | Purpose, Sustenance: satisfied |
| 600–839 | Afternoon | Physic's house | Patients. | |
| 840–899 | Afternoon | Walks toward Watch House | Fourth attempt. Gets to the step. Sits on the step for 20 minutes. Returns. | Safety: HIGH |
| 960–1199 | Evening | Temple | Evening prayer. Shorter today. | |
| 1200–1439 | Night | Physic's house | Sleep. Restless. | |

---

#### Day 4

| Ticks | Period | Location | Activity | Need State |
|-------|--------|----------|----------|------------|
| 240–599 | Morning | Physic's house / rounds | Patients. | |
| 600–839 | Afternoon | Physic's house | Patients. | |
| 840–959 | Afternoon | Physic's house | Stays in. Watches the street. | Safety: elevated |
| 960–1199 | Evening | Physic's house | Does not go to Temple. Sits with her notes. | Purpose: active |
| 1200–1439 | Night | Physic's house | Poor sleep. Knows she has to do it tomorrow. | |

---

#### Day 5

| Ticks | Period | Location | Activity | Need State |
|-------|--------|----------|----------|------------|
| 240–399 | Morning | Physic's house | Morning routine | |
| 400–479 | Morning | Physic's house | Servant arrives with Harro's "invitation." | Safety: CRITICAL — the threat resolves the paralysis |
| 480–500 | Morning | Physic's house → Watch House | Walks directly. No hesitation. Fifth approach, first completion. | Safety: overriding everything in the opposite direction |
| 500–599 | Morning | Watch House | Gives Wulf her full examination findings. Formal statement. | Purpose: fully released |

**Beat 9 (Harro overreach) fires the completion.** Harro's threat was intended to prevent disclosure. It caused it.

| 600–1199 | Afternoon/Evening | Physic's house | Patients. Routine. | All needs: normalized |
| 1200–1439 | Night | Physic's house | First full night of sleep since Day 0. | Rest: full |

#### Days 6–7: Full return to baseline. Schedule engine: normal execution. Disruption counter resets to 0.

---

### WULF DANE — Watch Captain
*The investigation pacing mechanism. His frustration is a need-state DramaManager reads to time beat escalations.*

**Baseline:** Up before dawn. At the Watch House before any soldier. Last to leave. Eats at his desk. Believes in procedure and is watching procedure be destroyed around him.

---

#### Day 1

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 60–239 | Dawn | Watch House | Reviewed the body report. Assigned Alys and Edda to the crime scene. | Purpose: HIGH | Was told of the body at tick 0. Did not sleep after. |
| 240–479 | Morning | Crime scene → Watch House | Initial investigation. | Purpose: HIGH | Collects physical observations. Notes access points from the manor side. |
| 480–599 | Morning | Watch House | Receives Harro Vane's "cooperative" visit. | Purpose: active, Safety: low-level alert | Something about Harro's precision bothers him. Cannot name it. |
| 600–839 | Afternoon | Watch House / interviews | Interviews Oskar Grain. Notes depth of fear. | Purpose: active | |
| 840–959 | Afternoon | Watch House | Receives Harro's Bastian Thorn suggestion. Suspicious of anonymous tips but professionally obligated. | Purpose: active, Frustration: rising | |
| 960–1079 | Evening | Watch House | Briefing with Alys. | Purpose: active | |
| 1080–1199 | Evening | Watch House | Reviews. At his desk. | | |
| 1200–1439 | Night | Watch House — cot | Sleep, broken | Rest: partial | Does not fully leave. Never does during active investigations. |

---

#### Day 2

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–399 | Morning | Watch House | Sends Alys to interview Bastian Thorn. | Purpose: active | Professionally obligated. Personally skeptical. |
| 400–479 | Morning | Crime scene | Returns to the river. Looks at the manor-side bank. | Purpose: active | Same spatial reasoning Cael is doing independently. They are converging. |
| 480–599 | Morning | Watch House | Dock standoff report arrives. | Purpose: HIGH | Beat 4. He goes to the docks and de-escalates. Furious at both factions. Notes the Guild has Curwin there under orders. |
| 600–839 | Afternoon | Watch House | Waits for Alys's Bastian Thorn report. | Purpose: active, Frustration: elevated | |
| 840–959 | Afternoon | Watch House | Alys returns. Bastian's alibi is solid. The false trail is dead. | Purpose: HIGH, Frustration: HIGH | He now knows someone pointed him at Bastian deliberately. He does not know who or why. Yet. |
| 960–1079 | Evening | Watch House | Formal request to review manor livery records. | Purpose: active | This request will be intercepted by Harro on Day 4. He doesn't know that yet. |
| 1080–1439 | Night | Watch House | Works late. Broken sleep. | Rest: degraded | |

---

#### Day 3

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–479 | Morning | Watch House | Reviews everything. No new leads. | Purpose: active, Frustration: HIGH | |
| 480–599 | Morning | Watch House | Harro's second cooperative visit. | Frustration: CRITICAL | Wulf is visibly colder than Day 1. Harro notes this. |
| 600–839 | Afternoon | Watch House | Handles the Piers Knot harassment operation against Cael. Dismisses it in under 40 ticks. | Purpose: active | Beat 6 side effect. He notes someone in the manor is running harassment ops against the stranger. |
| 840–959 | Afternoon | Watch House / soldier rounds | Checks patrol reports. Notes route irregularities. | Purpose: active | He does not yet know it's Piers. |
| 960–1079 | Evening | Watch House | Briefing with Alys. | Purpose: active | |
| 1080–1439 | Night | Watch House | Working late again. | Rest: degraded | |

---

#### Day 4

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–479 | Morning | Watch House | Follows up on the livery records request. No response from the manor. | Purpose: HIGH, Frustration: CRITICAL | He realizes the request has been intercepted. First time he formally suspects obstruction from inside the manor chain. |
| 480–599 | Morning | Watch House | Alys brings the button. | Purpose: HIGH | Beat 7. Physical evidence. Manor livery at the crime scene. |
| 600–719 | Afternoon | Watch House | Formal request to examine manor livery records. Goes through official channels. | Purpose: active | Harro has already left the manor by the time this arrives. |
| 720–839 | Afternoon | Watch House | Oona's testimony arrives — she came directly. | Purpose: HIGH | Beat 9 precursor. He now has physical evidence and medical evidence. Still no name. |
| 840–959 | Afternoon | Watch House | Begins building the warrant. Unnamed manor servant. | Purpose: HIGH | |
| 960–1439 | Evening/Night | Watch House | At his desk. | Rest: minimal | |

---

#### Day 5

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 480–540 | Morning | Watch House | Dame Isolt arrives with Lady Maren's message. | Purpose: CRITICAL | Beat 10. He has a name. |
| 540–600 | Morning | Watch House | Warrant issued. Assigns Alys and Edda. Goes to garrison directly — not manor. | Purpose: CRITICAL | He does not tell Edric. He does not trust the chain. |
| 600–840 | Afternoon | Watch House | Cael arrives. Full truth laid out. | Purpose: CRITICAL | Beat 12. He receives everything: witness chain, spatial analysis, the route Harro will have taken. |
| 840–1439 | Afternoon/Night | Watch House | Coordinates the road pursuit. Waits. | Purpose: active, Rest: abandoned | |

---

#### Day 6

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–839 | Morning/Afternoon | Watch House | Waits for Alys. Manages the faction chaos. Denies Guild access to Dole's ledgers pending Crown instruction. | Purpose: active | |
| 840–1199 | Evening | Watch House | Alys and Edda return with Harro. | Purpose: resolving | Beat 11 aftermath. |
| 1200–1439 | Night | Watch House | Brief sleep. First in five days. | Rest: partial | |

---

#### Day 7

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–479 | Morning | Watch House | Harro's confession. Edric present. | Purpose: complete | Beat 13. |
| 480–1439 | Afternoon–Night | Watch House | Reports. Documentation. Crown dispatch. | Purpose: satisfied | For the first time since Day 0, his schedule executes normally. |

---

### PIP FARR — Apprentice (age 16)
*Safety need drives everything. The first witness to reach Cael. His approach on Day 3 is the first direct testimony in the truth assembly.*

---

#### Days 1–2: Suppressed baseline

Tobias assigns him extra work both days — deliberate schedule loading to keep him occupied and visible under Guild supervision. He runs cloth deliveries all morning, sweeps the Merchant Hall all afternoon, eats with the Guild apprentices in the evening. He does not go near the river. He does not tell anyone what he saw.

**Need state across Days 1–2:** Safety maxed. Overrides everything. He is not sleeping well. He is not eating full meals. Other apprentices notice he is quiet.

---

#### Day 3

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–599 | Morning | Merchant Hall / deliveries | Normal assigned work | Safety: maxed |
| 600–659 | Afternoon | Crossed Keys — common room | Delivery to Brina. Notes Cael is there. Tobias is at the Merchant Hall. | Safety: maxed, Purpose: rising conflict |
| 660–700 | Afternoon | Crossed Keys — corner | Approaches Cael. Describes the man at the river. Does not know the name. | Safety: cracking | Beat 5 fires. He chose the one person Tobias said nothing about. |
| 700–719 | Afternoon | Crossed Keys | Cael arranges cover role with Brina. Pip is given a job before he can leave. | Safety: partially resolved | He didn't expect to feel safer. He does. |
| 720–959 | Afternoon | Crossed Keys — assigned tasks | Runs tavern errands. Visible. Busy. Protected. | Safety: recovering |

#### Days 4–7: Tavern routine. Safety need declines steadily. By Day 7 Evening he has returned to his full normal apprentice baseline. Brina notes this.

---

### FINN BARGE — Ferryman
*Safety need as a gate. Will not speak until the safety calculation shifts. The slowest witness to come forward.*

---

#### Days 1–4: Compressed baseline

Each day is nearly identical: dawn ferry preparation, morning crossings, afternoon maintenance, evening — he eats alone at the dock house, does not go to the tavern, does not go to the market. He has removed himself from social circulation entirely.

**Safety need:** Maxed from Day 0. His schedule is the most contracted in the sim. Every optional social activity has been eliminated. He is running the minimum required interactions to maintain his livelihood.

**Perceptual anomaly for other NPCs:** Characters who knew his pre-murder routine notice he is no longer present at his usual evening spots. This is a visible signal to anyone with enough social perception — Brina notices on Day 2, Cael notes the absence on Day 3.

---

#### Day 4

| Ticks | Period | Location | Activity | Need State | Notes |
|-------|--------|----------|----------|------------|-------|
| 240–839 | Morning/Afternoon | Dock / ferry | Normal work | Safety: HIGH |
| 840–899 | Afternoon | Walks toward Temple | Safety calculation shifting. Harro's harassment op read as desperation. Curwin's dock restrictions read as fear. Both factions are scared. | Safety: recalculating |
| 900–959 | Afternoon | Temple — exterior | Sits outside. Watches who enters. | Safety: recalculating |
| 960–1019 | Evening | Temple — interior | No faction members inside. Approaches Brother Cael. | Safety: threshold crossed |
| 1020–1079 | Evening | Temple | Tells Brother Cael about the boat. Manor-side. No lantern. Returned before dawn. Whose boat. | Purpose: released, Safety: partially resolved |
| 1080–1439 | Night | Dock house | Sleep. Still poor. But something has shifted. | Rest: partial |

**Beat 8 fires at tick ~960–1079.**

#### Days 5–7: Safety need slowly declining. By Day 7 he has resumed his usual evening at the Crossed Keys. First time since Day 0.

---

### COOK MARTA DELL — Manor Kitchen
*One of the four witnesses. Her information reached Lady Maren directly. Harro is keeping her inside the manor without knowing exactly why — he knows she knows something.*

---

#### Days 1–7: Manor-bound baseline

Her schedule is the most constrained of any character not in custody. She is physically within the manor grounds for all seven days.

**Day 1–2:** Normal kitchen routine. She told Lady Maren on Day 1 Morning that Rowan's alibi was false. Lady Maren told her to say nothing to anyone else and to stay visible.

**Day 3 onward:** Harro has arranged that her off-day (normally Day 4) is "postponed for household reasons." She does not know this is deliberate. She is becoming suspicious.

**Need state:** Safety rising from Day 3. She feels watched without being able to say why. Lady Maren visits the kitchen each morning — longer than usual. Marta understands this is protection.

**Day 7:** When the confession happens and Harro is in custody, her schedule normalizes within the same afternoon. She is the first character in the manor whose routine fully resets on Day 7.

---

### GROOM ALDRIC HOOF — Manor Stables
*The final witness. Total innocence. Delivers the confirmation on Day 5 Evening without knowing what he has said.*

---

#### Days 1–7: Completely stable baseline

Aldric's schedule is the most normal in the entire sim. He is the only witness whose routine does not deviate at any point across all seven days.

**Dawn:** Up before first light. Feeds horses.
**Morning:** Mucking, tack maintenance, exercise the lord's horses.
**Afternoon:** Farrier coordination, repairs, visitor horses.
**Evening:** Final feed. Checks each animal.
**Night:** Bed early. Up early tomorrow.

**Need state across all 7 days:** All needs satisfied at baseline. Safety never rises above nominal. He has not connected what he saw to anything significant.

**Day 5 Evening tick ~840–920:** Cael arrives for a casual visit. Asks about horse routines, what Aldric sees from the stable loft, whether he sleeps well. Aldric answers all of it, mentions Harro's unusual departure in the small hours because it was unusual — a mare had been restless and he'd been watching the yard. He connects it to nothing. He is already talking about the mare's recovery before Cael's expression settles.

**This is the scene.** The most important 80 ticks in the sim, delivered by the character with the flattest affect vector and the most stable schedule.

---

## TIER 2 — Baseline + Disruption Notes
*Daily rhythm summary · disruption triggers per beat*

---

### VESPERA HOLT — Guild Master

**Baseline rhythm:**
- Morning: Merchant Hall, briefings with Draven and Mira
- Afternoon: Market business, audience hours for Guild members
- Evening: Crossed Keys — present but not drinking, gathering intelligence
- Night: Merchant Hall private quarters

**Needs:** Purpose dominant. Safety rises Day 2 onward. Social high — she requires information flow the way other characters require food.

**Disruption triggers:**
- **Beat 1** (Cael's commission) → redirects Morning briefing to threat assessment. Sela Torch dispatched.
- **Beat 2** (Guild counter-offer accepted) → Safety partially resolved. Draven given expanded mandate.
- **Day 3** (Beck false rumor reaches her) → Draven taken off dock ops, assigned reputation management. Dock access loosens — Cael's intended effect.
- **Day 5** (Cael's false Harro-blackmail rumor) → Draven redirected again. Vespera now running two operations on false intelligence.
- **Day 6** (ledgers surface) → Pell Wynne immediately activated. Legal argument construction begins. Mira Ashby identified as the sacrifice.
- **Day 7** → Cooperates with Crown investigation first. Survives. Schedule normal by Day 7 Evening.

---

### DRAVEN COLE — Guild Fixer

**Baseline rhythm:**
- Dawn: Warehouse district, meeting Curwin before the market opens
- Morning: Dock operations, logistics
- Afternoon: Variable — wherever Vespera's priorities require
- Evening: Crossed Keys, intelligence gathering
- Night: Warehouse district private room

**Needs:** Purpose dominant. Safety low — he assesses threats professionally and doesn't panic.

**Disruption triggers:**
- **Day 1 Evening** (Beck rumor implicates him) → Visible energy managing reputation. Exactly what Cael wanted.
- **Day 2** (dock standoff, Beat 4) → SquadBlackboard: HOSTILE_CONTACT logged. Redirected by Vespera.
- **Day 3** (redirected off docks by Vespera after Beck rumor propagates) → Dock access loosens.
- **Day 5** (Cael's false blackmail rumor) → Redirected to ledger hunt. Off dock ops entirely.
- **Day 6** (ledgers found, Mira sacrificed) → Draven is the one who could have helped Vespera and doesn't. He saw what Cael did. Quiet respect. Does not obstruct.

---

### ALYS BRAND — Watch Sergeant

**Baseline rhythm:**
- Dawn: Watch House, briefing with Wulf before the day begins
- Morning: Field work — crime scene, interviews, patrol management
- Afternoon: Watch House, reports, coordination
- Evening: Watch House — she stays late
- Night: Watch House — brief sleep on the cot

**Needs:** Purpose dominant. Frustration rises as a secondary need-state analog.

**Disruption triggers:**
- **Day 2** (assigned to Bastian Thorn interview) → Schedule rerouted to his farm. Returns with alibi solid. Fury.
- **Day 4 Morning** (Suki gives her the button) → Beat 7. Schedule pivots to evidence chain.
- **Day 5** (Harro named, warrant issued) → Schedule: locate and secure. Does not announce.
- **Day 6 Evening** (returns with Harro in custody) → First complete task execution since Day 0.

---

### LADY MAREN ASHFORD

**Baseline rhythm:**
- Morning: Manor — private correspondence, then manages household alongside Harro (from distance)
- Afternoon: Temple visit (daily, longer than social expectation requires)
- Evening: Manor — with Edric, managing what he knows and doesn't know
- Night: Manor — private quarters

**Needs:** Purpose dominant. Safety elevated for the House — she distinguishes this from personal safety. Social constrained — she speaks carefully to everyone.

**Disruption triggers:**
- **Day 1** (Cook Marta's information) → Quiet panic, resolved into action within the same afternoon. Dispatches Dame Isolt.
- **Day 2** (Isolt's report on Harro's departure) → `maren.belief.harro.suspicious = TRUE`. Does not tell Edric.
- **Day 4** (Isolt confirms via Aldric) → Decision made: controlled demolition.
- **Day 5** (sends Isolt to Wulf with Harro's name) → Beat 10. She does not tell Edric she did this until the confession is happening.
- **Day 7** → Drafts Edric's Crown offer before the confession. Already managing what comes next.

---

### ROWAN ASHFORD — Lord's Son

**Baseline (pre-murder):** Active, present in the market and with peers. Now: restricted to manor, erratic pattern, visible affect anomaly.

**Needs:** Safety extreme. Guilt functions as a chronic need-state distortion — every other need is suppressed or distorted by it.

**Disruption triggers:**
- **Day 1–4:** Schedule shows increasing contraction. Fewer rooms visited. Fewer meals attended. Manor staff notice.
- **Day 3 Night** (walks to Temple, speaks with Brother Cael without confessing) → Edda Helm observes. Rowan returns no more resolved than before.
- **Day 5** (Harro's departure) → Rowan does not know Harro has gone until morning. When he learns: the one person managing his secret is gone. AffectVector: panic spike.
- **Day 7** (hears Harro's confession) → AffectVector: `guilt = ACUTE → CHRONIC`. Not relief. The end of the specific horror of not knowing who knew.

---

### LORD EDRIC ASHFORD

**Baseline:** Lord's routine. Manor briefings, land management, Watch House visits on dispute days. Evening meal with household.

**Needs:** Purpose (authority) dominant. Safety elevated for the House — he processes this as protecting Rowan, which is not the same thing.

**Disruption triggers:**
- **Day 1** (body found) → Schedule rerouted to crisis management. Commissions Cael as visible action.
- **Day 2–4:** Surface normal. Internal: running a story that is increasingly inconsistent with events.
- **Day 5** (learns the Watch has issued a warrant) → Lady Maren tells him. He does not know she named Harro. He goes to the Watch House. Wulf meets him there and tells him nothing useful.
- **Day 7** (attends confession per Maren's instruction) → Does not speak during Harro's statement. Ages visibly. Schedule engine: no entries for the rest of the day.

---

### BROTHER CAEL — Temple Monk

**Baseline rhythm:**
- Dawn: Morning office (prayers)
- Morning: Temple — open for visitors
- Afternoon: Temple — counseling, records
- Evening: Evening office, then available
- Night: Temple quarters

**Needs:** Purpose dominant. Social extremely high — he processes information through pastoral relationships. Safety rises as he accumulates information he cannot safely hold.

**Disruption triggers:**
- **Day 2** (Oona's disclosure) → Beat 3. Holds medical evidence. Safety rising.
- **Day 2** (notices Silas Pen's hidden notes) → Holds household records evidence. Safety rising further.
- **Day 3 Night** (Rowan's visit) → Recognizes the affect profile of specific guilt. Does not know who or what yet.
- **Day 4** (Finn's testimony) → Beat 8. Now holds everything. Safety at maximum. Cannot hold this alone.
- **Day 4 Evening** (summons Cael Mourne) → Transfers everything to Cael. Safety resolves. Purpose fully satisfied.
- **Day 7** → Schedule fully normalized. He knows the outcome. He asks no questions about how.

---

### BRINA MAST — Innkeeper

**Baseline:** Crossed Keys open from Morning through Night. She is always present. She is the tavern.

**Needs:** Social and Purpose balanced. She does not want trouble but she wants to know everything.

**Disruption triggers:**
- **Day 1** (Cael arrives) → Logs the anomaly — asks nothing about the murder. Files it.
- **Day 3** (Cael asks her to shelter Pip) → She says yes without asking why. She has seen enough to know why.
- **Day 4–7** → Runs Pip's cover. Keeps him visible and busy. Tells no one. Her schedule does not change. Everything around her does.

---

### HARLAND BECK — Grain Merchant

**Baseline:** Market stall Morning–Afternoon. Crossed Keys Evening. Regular, social, talkative.

**Disruption triggers:**
- **Day 1 Evening** → Seeds false rumor (Draven did it) to Hilde Basket. Schedule: normal. He believes what he's saying. This is not malice. This is overconfident reasoning.
- **Days 2–4** → As the rumor propagates and degrades, characters with higher fidelity information begin contradicting it. His social comfort declines. He defends his theory with decreasing confidence.
- **Day 6–7** → Rumor fidelity 0.06. Three characters still carry it. He has largely stopped mentioning it. Does not know he was wrong. Will never fully know.

---

### HILDE BASKET — Market Vendor

**Baseline:** Market stall all day. Wanders Workers' Quarter Evening. Maximum social contact, minimum discretion.

**Disruption triggers:**
- **Day 1 Evening** → Receives Beck's rumor (Draven did it). Propagates within 5 ticks to anyone nearby. Adds embellishment: Draven was seen near the river (false — her addition).
- **Days 2–4** → Propagates every new piece of information she receives with equal enthusiasm and equal fidelity degradation. Her version of any fact is worth approximately 0.4 of the original.
- **Day 5** (Pell Wynne's competing rumor reaches her) → She now propagates two conflicting theories simultaneously. Characters who hear both from her: information value approaches zero.

---

### PIERS KNOT — Guard (corrupt)

**Baseline:** Patrol schedule assigned by Watch duty roster. Evening: above-wages spending visible at the Crossed Keys.

**Disruption triggers:**
- **Day 2** (assigned new patrol routes by Harro via private meeting) → Reroutes his own patrol to keep Watch away from the river at Night.
- **Day 3** (executes Cael harassment op) → Files a false theft report. Wulf dismisses within 40 ticks.
- **Day 5** (receives and delivers warrant intel to Harro) → Harro's final useful act from Piers. Tips Harro about the warrant at tick ~900.
- **Day 5–6** (Harro flees) → Piers is now running cleanup operations with no handler. Increasingly erratic.
- **Day 6** (Astrid Loom's intelligence about his spending reaches Wulf) → Wulf pulls him from active duty. Investigation pending.

---

### SUKI CROSS — Guard

**Baseline:** Standard Watch patrol. Methodical. Thorough. Uncomfortable with disorder.

**Disruption triggers:**
- **Day 1** (finds the button at the river during the initial crime scene work) → Does not report. Does not know who to trust.
- **Days 2–3** → Sits with it. Watches who she can trust. Notes Alys Brand's behavior — frustrated, honest, being blocked.
- **Day 4 Morning** → Gives it to Alys. Not Wulf — Alys. Beat 7 fires. `cael.belief.suspect = HARRO_VANE` converges with the physical evidence from an independent thread.
- **Day 4–7** → Returns to normal patrol. Professional satisfaction: quiet, complete.

---

## TIER 3 — Archetype Templates
*Template reference · character-specific deviation*

---

### Archetype: MERCHANT ROUTINE

**Daily pattern:**
- Dawn: home/lodgings
- Morning: stall or warehouse, trade hours
- Afternoon: market, Guild business if applicable
- Evening: Crossed Keys or home
- Night: home/lodgings

**Applies to:** Tobias Farr (05), Catrin Luce (06), Oswin Salt (07), Harland Beck (09) — already in Tier 2

**Character deviations:**

**Tobias Farr (05):** Morning schedule includes supervising Pip's work — deliberate. Evening at Crossed Keys is louder and more visible than useful. After Day 3 he notes Pip is spending time at the tavern without being able to object. His frustration registers as elevated social aggression toward anyone near Pip.

**Catrin Luce (06):** Afternoon sometimes includes a visit to the Manor district — debt management. These visits become more frequent from Day 2 as both factions try to leverage her dual loyalty. Her schedule shows the stress as extended visit durations and delayed returns to her stall.

**Oswin Salt (07):** Baseline most stable of any merchant. Deliberately. He has survived three political crises by being the least interesting person in every room. His schedule does not deviate. His believe model does — by Day 4 it is substantially disrupted, invisible to anyone watching his routine.

---

### Archetype: GUILD SPECIALIST ROUTINE

**Daily pattern:**
- Morning: Guild district, specific professional function
- Afternoon: Guild Hall or assigned territory
- Evening: Crossed Keys or Guild Hall
- Night: Guild district lodgings

**Applies to:** Sela Torch (10), Aldous Ink (14), Mott Wheel (15), Astrid Loom (19)

**Character deviations:**

**Sela Torch (10):** Morning includes courier dispatch — she moves through the whole town during the first two hours. Her schedule gives her the widest spatial coverage of any character. The intelligence network she feeds is the fastest-propagating legitimate information channel in the sim.

**Aldous Ink (14):** Afternoon increasingly involves trying to look like his archive is less significant than it is. Both factions attempt access on Days 3 and 4. He refuses both with Guild procedural arguments. His schedule does not change. His safety need rises steadily.

**Mott Wheel (15):** Schedule is the most geographically distributed in the sim — workshops, both faction districts, the road. Day 6: all three factions contact him within 90 ticks about Harro's transport. He tells all of them the truth simultaneously. ReputationEngine multi-faction crash. His schedule on Day 7 is invisible — he does not leave his workshop.

**Astrid Loom (19):** Schedule appears fully residential — Workers' Quarter, weaving, market errands. Actually covers most of the town through "shopping." Tracks Piers Knot's spending from Day 2. Delivers the intelligence to Vespera on Day 5, which Vespera passes to Wulf on Day 6.

---

### Archetype: WATCH SOLDIER ROUTINE

**Daily pattern:**
- Dawn: Watch House, roll call
- Morning: assigned patrol
- Afternoon: patrol or Watch House standby
- Evening: Watch House, end of shift — most go to Crossed Keys briefly
- Night: barracks

**Applies to:** Edda Helm (31), Bram Spear (33) — Alys and Suki already tiered

**Character deviations:**

**Edda Helm (31):** Afternoon patrol has been quietly modified — she routes herself near Rowan Ashford's expected locations. Unofficial. Wulf knows she does this and has not stopped her. Day 3 Night: deviates from barracks to follow Rowan to the Temple. Day 6 Evening: rides south with Alys.

**Bram Spear (33):** Assigned trivial tasks — supply inventory, gate logging — that keep him away from the investigation. His schedule is the most tightly controlled by external assignment of any soldier. He follows procedure exactly because he doesn't know enough to do otherwise. On Day 5, following procedure, he nearly logs an observation about Harro's departure before being redirected by Piers. The log entry is incomplete. Wulf finds it on Day 7.

---

### Archetype: NOBLE HOUSE ROUTINE

**Daily pattern:**
- Morning: Manor, role-specific functions
- Afternoon: Manor or supervised external
- Evening: Manor household
- Night: Manor

**Applies to:** Dame Isolt Creed (26), Reeve Bastian Thorn (27), Clerk Silas Pen (28)

**Character deviations:**

**Dame Isolt Creed (26):** Afternoon schedule increasingly off-manor as Lady Maren's investigation expands. Visits look like normal knight's business. By Day 4 she has spoken with Groom Aldric, Cook Marta, and three Watch soldiers without any of them knowing they were being interviewed. Day 5: delivers Harro's name to Wulf.

**Reeve Bastian Thorn (27):** Normal land management schedule disrupted by Alys's interview on Day 2. Returns to normal Day 3. By Day 4 he realizes his name was floated as a suspect. His afternoon schedule now includes increasingly anxious visits to the Watch House to check the status of the investigation. Wulf is professionally reassuring and privately not concerned with him at all.

**Clerk Silas Pen (28):** Schedule includes after-hours Temple visits beginning Day 1 — hiding notes. By Day 4 this has become a daily deviation: normal Manor work, then a late-afternoon Temple visit that takes longer than prayer requires. Brother Cael knows he is hiding something there. Does not read the notes until Oona's disclosure gives him the context to understand them.

---

### Archetype: MANOR SERVANT ROUTINE

**Daily pattern:**
- Dawn: kitchen or stables (role-specific)
- Morning: primary duties
- Afternoon: secondary duties, manor grounds
- Evening: servants' hall, early night
- Night: servants' quarters

**Applies to:** Cook Marta Dell (35) — already Tier 1, Groom Aldric Hoof (36) — already Tier 1

*(These two are Tier 1. Listed here for completeness.)*

---

### Archetype: INDEPENDENT PRACTITIONER

**Daily pattern:**
- Morning: professional function (healing, temple, trade)
- Afternoon: professional function
- Evening: community presence
- Night: own dwelling

**Applies to:** Hedgewitch Vorn (40)

**Character deviation:**

**Hedgewitch Vorn (40):** Lives outside town. Her schedule includes a daily Morning walk into town that passes both faction districts. Both factions have separately asked her if she saw river traffic on the murder night. She did. She saw a boat without a lantern. She has told neither faction because neither asked correctly — they asked "did you see anything suspicious" and she answered "no" because she did not know at the time that it was suspicious. Cael never approaches her. She would have told him if he had. This is the one thread that does not resolve. By Day 7, the information is no longer needed. It sits with her, unspoken, permanently.

---

### Archetype: TENANT / CRAFTSMAN ROUTINE

**Daily pattern:**
- Morning: craft or farm work
- Afternoon: market or deliveries
- Evening: Workers' Quarter social or home
- Night: home

**Applies to:** Tanner Bret Hide (39), Tenant Ffion Dirt (38)

**Character deviations:**

**Tanner Bret Hide (39):** Evening schedule includes a mandatory report to the Watch House — not daily, but every other day. Structured as a "checking in." He hates this. His affect model shows low-level chronic distress throughout the sim. By Day 5 both factions know he's an informant and neither trusts anything that came through him.

**Tenant Ffion Dirt (38):** Not in town. Her schedule is farmstead-based throughout. Both factions send riders on Days 3 and 4 respectively. The Guild rider arrives first but she is in her fields — the rider does not wait. The Watch rider arrives Day 4 and leaves a message with a neighbor. She does not come to town until Day 6, by which point her testimony is no longer the most urgent thread. Wulf interviews her Day 7. Her information is useful for the Crown record. The story was over before she arrived.

---

## Schedule System Notes

**ScheduleEngine anomaly signals — key detection patterns:**

| Character | Anomaly Type | Visible From | Signals |
|-----------|-------------|--------------|---------|
| Harro Vane | PATTERN_BREAK | Day 6 tick 1440 | Room empty, schedule silent |
| Oona | Repeated partial execution | Day 1 onward | Walk to Watch House incomplete 4 times |
| Oskar Grain | Full contraction | Day 1 onward | Never leaves mill, misses market days |
| Mira Ashby | Routine rigidity | Day 1 onward | Schedule too fixed, fear masking as order |
| Rowan Ashford | Progressive contraction | Day 1 onward | Fewer locations per day, fewer meals |
| Mott Wheel | Multi-faction collision | Day 6 | All three contact him within 90 ticks |
| Cael Mourne | Absence of routine | Day 1 onward | No predictable pattern — anomaly for Harro |

**Need-stack priority resolution (all characters):**
Safety > Sustenance > Rest > Social > Purpose

When Safety is maxed, all lower needs become inaccessible to the scheduler. Finn's 4-day social contraction, Oskar's missed meals, and Oona's failed walks are all Safety-override events at different threshold levels.

**DramaManager reading schedule data:**
Beats 3, 5, 8, and 11 fire on schedule-derived conditions rather than direct event triggers. The ScheduleEngine is the primary input for DramaManager's witness-safety metric and investigation-momentum metric throughout the run.