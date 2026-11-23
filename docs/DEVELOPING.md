# Developing RyanCraft

**Building:** needs Java 21. `./gradlew build`; the jar lands in `build/libs/`.

**Art, sound and data:** almost every texture, model, sound and JSON file is made by a generator in `tools/`.
Start with `python3 tools/resources.py` for the JSON data and language file, and `python3 tools/critters.py` for
mob models and textures. Each other script (for example `tools/skylands.py`, `tools/cooking.py`) builds one part
of the mod and says at the top what it makes.

**Releasing:** bump `mod_version` in `gradle.properties` and add a section for that version to
[CHANGELOG.md](CHANGELOG.md). Pushing that commit publishes a GitHub Release automatically, with the jar attached
and that changelog section as the notes.

**Lessons learned:** [AAR.md](AAR.md) collects testing tricks and pitfalls found along the way.
