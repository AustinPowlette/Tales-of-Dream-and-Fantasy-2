Kept songs live here: one text score per .txt file, read when the game starts (no recompile).
  music gen [open|city|danger|night|war|sect|capital] [seed]   composes a song and plays it
  music keep                                                   saves the song that is playing in this folder
  music drop <song>                                            deletes a kept song
A file is an ordinary song score (the format is described in Game Info.md, 'Music'): you may edit a kept song by hand or write your own.
A file that does not parse is skipped (the reason is in SongBook.keptProblems). Every kept song needs a 'moods:' line (mood:weight, 0 open, 1 city, 2 danger, 3 night, 4 war, 5 sect, 6 capital).
(Song composing, 'music gen', is an experimental extra; the songs of the game are written by hand.)
