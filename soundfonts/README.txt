Put SoundFont files (.sf2, .dls) here, then in the game: music engine soundfont   (or Settings > Sound source).
music fonts      - list files
music presets    - list the presets inside the chosen font
Optional programs.txt maps instruments to presets, e.g.:
  pluck=107
  erhu=0:110
Instruments: pluck flute erhu pad bell chime taiko wood pipa
Fifteen instruments are used (pluck flute erhu pad bell chime taiko wood pipa piano harp strings cello horn choir). MidiOut.TRIM_DB holds the loudness trims measured for GeneralUser-GS.sf2; for another font run SongTool trim and put the new numbers there.
