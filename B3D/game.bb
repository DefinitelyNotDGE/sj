Global cur_scr_num

For i = 1 To 999
	If i < 10
		scr_suf$ = Str$("00"+i)
	ElseIf i < 100
		scr_suf$ = Str$("0"+i)
	Else
		scr_suf$ = Str$(i)
	EndIf
	scr = LoadImage("screenshot_"+scr_suf$+".bmp")
	If scr = 0 Then
		cur_scr_num = i
		Exit
	EndIf
Next



Global server_ip$,server_message$
player_user_name$ = "UNDEFINED"			;this used to grab the players name from the users name on windows, let's not do that


AppTitle "Sisko Jump!"

Graphics 400,600,0,2
SetBuffer BackBuffer()


Global current_sec
Global fps_rate,fps_rate_old

Global hsf,xpl,ypl
Global serv_update = 0

Dim highscore_line$(60)
Dim highscore_score(60)
Dim highscore_name$(60)
Dim highscore_date$(60)

Global max_highscore_num = 60			;just some quick fixes to make a working local highscore
Global current_date$ = Mid$(CurrentTime(),1,5) + " / " + CurrentDate()

Global plshadow_cd

Global fontbig = LoadFont("comic sans ms",66,1,0,0)
Global fontmid = LoadFont("comic sans ms",26,0,1,0)
Global fontmid_nk = LoadFont("comic sans ms",24)
Global fontmid_nk_un = LoadFont("comic sans ms",24,0,0,1)
Global fontsmall = LoadFont("comic sans ms",20,1,0,0)
Global fontsmall2 = LoadFont("comic sans ms",17,1,0,0)
Global fontsmall3 = LoadFont("comic sans ms",10,1,0,0)
Global fontsmall_k = LoadFont("comic sans ms",20,1,1,0)
Global fontsmall_ch = LoadFont("comic sans ms",26,1,0,1)

Global fontmessage1 = LoadFont("comic sans ms",20,0,1,0)

Global fontvers1 = LoadFont("calvinital",20,1)
Global fontvers2 = LoadFont("calvinital",30,1)

Global fontmenu = LoadFont("comic sans ms",36,1,0,0)
Global fontmenu2 = LoadFont("comic sans ms",36,1,0,0)
Global fontmenu_un = LoadFont("comic sans ms",36,1,0,1)
Global fontmenu_ch = LoadFont("comic sans ms",42,1,0,1)

Global game_version$ = "1.0"

Global win_act = 0

Global graphic_level

SeedRnd(MilliSecs())

Global game_played = False


Dim TEXT_OUTPUT$(100)
TEXT_OUTPUT$(1) = "Mit dem Server verbinden?"
TEXT_OUTPUT$(2) = "JA!"
TEXT_OUTPUT$(3) = "nööö"

TEXT_OUTPUT$(4) = "ja"
TEXT_OUTPUT$(5) = "Nein!"
TEXT_OUTPUT$(6) = "Drücke Enter um neuzustarten (F5 für Upload)"
TEXT_OUTPUT$(7) = "Punkte: "
TEXT_OUTPUT$(8) = "Pause"
TEXT_OUTPUT$(9) = "Aufgeben"
TEXT_OUTPUT$(10) = "Start"
TEXT_OUTPUT$(11) = "Highscore"
TEXT_OUTPUT$(12) = "Optionen"
TEXT_OUTPUT$(13) = "ENDE"
TEXT_OUTPUT$(14) = "Grafik:"
TEXT_OUTPUT$(15) = "1.Paint FTW!"
TEXT_OUTPUT$(16) = "2.Rects"
TEXT_OUTPUT$(17) = "3.ELMARIEL"
TEXT_OUTPUT$(18) = "Leaven?!"
TEXT_OUTPUT$(19) = "Du bist nicht mit dem Server verbunden!"
TEXT_OUTPUT$(20) = "Superjump: "

TEXT_OUTPUT$(21) = "Gebe deinen Namen ein: "
TEXT_OUTPUT$(22) = "Gebe einen GÜLTIGEN Namen (ohne runde Klammern!) ein!"
TEXT_OUTPUT$(23) = "IP-Server ist down. Versuchs mit dem Uploaden später!"
TEXT_OUTPUT$(24) = "Highscore-Server ist down. Versuchs mit dem Uploaden später!"

TEXT_OUTPUT$(25) = "Wabosh sucks!"

Global bar_size = 50, bar_wide = 10, bar_r = 20

Dim SERVER_MSG$(200)
Dim SERVER_MSG_X(200)


Global KEY_TAB = 15
Global KEY_ESC = 1
Global KEY_SPACE = 57
Global KEY_ENTER = 28
Global KEY_1 = 2
Global KEY_A = 30
Global KEY_D = 32
Global KEY_LEFT = 203
Global KEY_RIGHT = 205
Global KEY_F5 = 63




Type bar
	Field bx
	Field by
	Field box
	Field boy
	Field bmove
	Field bkind
	Field bactive
	Field btime
	Field bframe
End Type


Dim bar_pic(10)
For i = 1 To 7
	bar_pic(i) = LoadImage("gfx\bar"+Str$(i)+".bmp")
	If bar_pic(i) <> 0
		bar_size = ImageWidth(bar_pic(i))
		bar_wide = ImageHeight(bar_pic(i))
	EndIf
Next


;Testing stuff
Type ArchiveImage
	Field AIsx,AIsy
	Field AIImage
	Field AIFrame[1024]
	Field AIName$
	Field AIAName$
	Field AIMaxFrames
End Type

;InitAI("gfx.ai")

Global bar_image = LoadAnimImage("bar.bmp",50,10,0,16)
Global player_image = LoadAnimImage("player.bmp",46,49,0,4)
MaskImage player_image,255,0,255
Global fow = LoadImage("fow.bmp")
MidHandle fow
MaskImage fow,255,0,255

SetBuffer BackBuffer()
;end



Type obj
	Field ox
	Field oy
	Field okind
End Type

Type shadow
	Field px
	Field py
	Field ptime
End Type

Type star
	Field sx
	Field sy
End Type

Type quake
	Field qx,qbx
	Field qy
	Field qr,qg,qb
	Field qtime
End Type


Global gravity# = 0.11

Global player_x#, player_y#, player_size = 45
Global player_score
Global player_name$
Global player_condition
Global player_speed# = 1.5
Global player_jump, player_acc#
Global level_line, level_p
Global player_sp_jump = 1, player_sp_jump_added = 1
Global player_status,player_status_time

Global pl_fall
Global paused = False
Global player_moved = False

Global level_added = 1
Global already_asked = False
Global game_over

Global scroll_y, scroll_y_f


Global ftimer = CreateTimer(140)
Global ftimer2 = CreateTimer(142)




MainMenu()
.start

FlushKeys()

GetHighscore()
MainLoop()

Function MainLoop()
hsf2 = ReadFile("options")
If hsf2 = 0 Then
	hsf2 = WriteFile("options")
	WriteInt hsf2,0
	CloseFile hsf2
	hsf2 = OpenFile("options")
EndIf
graphic_level = ReadInt(hsf2)


ResetAll()
CreateLevel()

Repeat					;<- the actual loop
If level_added > 8
	WaitTimer ftimer2   ;makes the game faster as one reaches a certain score level?
Else
	WaitTimer ftimer
EndIf
Cls
SetFont fontsmall
DrawLevel()

If game_over = False
	If Not KeyDown(KEY_TAB) Then
		If paused = False And win_act = False
			MovePlayer()
		EndIf
	EndIf
Else
	Color 50,50,50
	Rect GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(6))/2-20,GraphicsHeight()/2-FontHeight()*2+5,StringWidth(TEXT_OUTPUT$(6))+40,FontHeight()*4,1
	Color 255,255,255
	Rect GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(6))/2-20,GraphicsHeight()/2-FontHeight()*2+5,StringWidth(TEXT_OUTPUT$(6))+40,FontHeight()*4,0
	Color 80,250,230
	Text GraphicsWidth()/2,GraphicsHeight()/2-FontHeight(),TEXT_OUTPUT$(7) + scroll_y_f,1
	Text GraphicsWidth()/2,GraphicsHeight()/2,TEXT_OUTPUT$(6),1
	If KeyHit(KEY_F5) Then		;it's game over and we wanna save and upload the highscore
		If player_score < scroll_y_f Then
			nphs = WriteFile("highscore")
			WriteInt nphs,scroll_y_f
			WriteString nphs,game_version$
			WriteString nphs,Mid(CurrentTime(),1,5) + " / " + CurrentDate()
			CloseFile nphs
		EndIf
		
		If serv_update Then
			GetHighscore()
		Else
			AddLocalHighscore(player_name$, player_score, current_date$)
		EndIf
		game_over = False
		MainLoop()
	EndIf						;we just save the highscore
	If KeyHit(KEY_ENTER) Or KeyHit(KEY_SPACE) Then
		If player_score < scroll_y_f Then
			nphs = WriteFile("highscore")
			WriteInt nphs,scroll_y_f
			WriteString nphs,game_version$
			WriteString nphs,Mid(CurrentTime(),1,5) + " / " + CurrentDate()
			CloseFile nphs
		EndIf
		SetLocalHighscore(player_name$, player_score, current_date$)
		game_over = False
		MainLoop()
	EndIf
EndIf


If KeyDown(KEY_TAB) Then		;displays highscore
	sw = 0
	For i = 1 To 20
		If StringWidth(highscore_line$(i)) > sw Then sw = StringWidth(highscore_line$(i))
	Next
	sw = sw*1.3

	Color 50,50,50
	Rect GraphicsWidth()/2-sw/2,55,sw,430,1
	Color 255,255,255
	Rect GraphicsWidth()/2-sw/2,55,sw,430,0
	Color 0,200,150
	For i = 1 To 20
		Color 200,0,150
		Text GraphicsWidth()/2 - sw/2 + 10,20*i+55,i+"."
		Color 0,200,150
		Text GraphicsWidth()/2,20*i+55,highscore_line$(i),1
	Next
EndIf
SetFont fontsmall

If KeyHit(25) Then				;pauses/unpauses the game
	paused = Not paused
EndIf
If paused = True Then
	Color 50,50,50
	Rect GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(8))/2-20,GraphicsHeight()/2-FontHeight()*2,StringWidth(TEXT_OUTPUT$(8))+40,FontHeight()*2,1
	Color 255,255,255
	Rect GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(8))/2-20,GraphicsHeight()/2-FontHeight()*2,StringWidth(TEXT_OUTPUT$(8))+40,FontHeight()*2,0
	Color 200,0,0
	Text GraphicsWidth()/2,GraphicsHeight()/2-FontHeight(),TEXT_OUTPUT$(8),1,1
EndIf

If KeyHit(KEY_ESC) Or MouseHit(2) Then			;ask to exit to menu
	win_act = Not win_act
EndIf
If win_act = True Then
	SetFont fontmenu
	Color 50,50,50
	Rect GraphicsWidth()/2-100,GraphicsHeight()/2-FontHeight()*3,200,FontHeight()*3,1
	Color 255,255,255
	Rect GraphicsWidth()/2-100,GraphicsHeight()/2-FontHeight()*3,200,FontHeight()*3,0
	Color 0,200,0
	If MouseX() > GraphicsWidth()/2 And MouseX() < GraphicsWidth()/2+StringWidth(TEXT_OUTPUT$(5))+20 And MouseY() > GraphicsHeight()/2-FontHeight()*2+20 And MouseY() < GraphicsHeight()/2-20
		SetFont fontmenu_ch
		If MouseHit(1) Then
			FlushKeys()
			FlushMouse()
			win_act = False
		EndIf
	Else
		SetFont fontmenu
	EndIf
	Text GraphicsWidth()/2+40,GraphicsHeight()/2-40,TEXT_OUTPUT$(5),1,1
	Color 200,0,0
	SetFont fontsmall
	If MouseX() > GraphicsWidth()/2-50-StringWidth(TEXT_OUTPUT$(4)) And MouseX() < GraphicsWidth()/2-35 And MouseY() > GraphicsHeight()/2-FontHeight()*2-10 And MouseY() < GraphicsHeight()/2-20
		SetFont fontsmall_ch
		If MouseHit(1) Then
			win_act = False
			ResetAll()
			MainMenu()
		EndIf
	Else
		SetFont fontsmall
	EndIf
	Text GraphicsWidth()/2-50,GraphicsHeight()/2-40,TEXT_OUTPUT$(4),1,1
	
	SetFont fontmid
	Color 0,200,250
	Text GraphicsWidth()/2,GraphicsHeight()/2-100,TEXT_OUTPUT$(9),1
	FlushMouse()
EndIf

Color 255,255,255
Flip 0

Forever
End Function




Function MainMenu()
GetPlayerName()
Delete Each star

;reads the "options" file - with a single option - to set the graphics level... (kek)
hsf2 = ReadFile("options")
If hsf2 = 0 Then
	hsf2 = WriteFile("options")
	WriteInt hsf2, 4
	CloseFile hsf2
	hsf2 = OpenFile("options")
EndIf
graphic_level = ReadInt(hsf2)


If serv_update = False And already_asked = False
Repeat
WaitTimer ftimer	;wait frame timer
Cls					;clear screen


;asks if we wanna connect to the server
Color 255,255,255
Rect GraphicsWidth()/2-152,GraphicsHeight()/2-GraphicsHeight()/4-2,304,124,1
If Not (MouseX() > GraphicsWidth()/2-150 And MouseX() < GraphicsWidth()/2-150 + 300 And MouseY() > GraphicsHeight()/2-GraphicsHeight()/4 And MouseY() < GraphicsHeight()/2-GraphicsHeight()/4 + 120) Then
	Color 0,150,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2-GraphicsHeight()/4,300,120,1
	Color 0,180,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2-GraphicsHeight()/4,300,60,1
	
	If MouseHit(1) Then
		serv_update = 0
		already_asked = True
		GetLocalHighscore()
		SaveLocalHighscore()
		Exit
	EndIf
Else	
	Color 0,180,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2-GraphicsHeight()/4,300,120,1
	Color 0,210,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2-GraphicsHeight()/4,300,60,1
EndIf

Color 255,255,255
Rect GraphicsWidth()/2-152,GraphicsHeight()/2+-2,304,124,1
If Not (MouseX() > GraphicsWidth()/2-150 And MouseX() < GraphicsWidth()/2-150 + 300 And MouseY() > GraphicsHeight()/2 And MouseY() < GraphicsHeight()/2 + 120) Then
	Color 150,0,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2,300,120,1
	Color 180,0,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2,300,60,1
	
	If MouseHit(1) Then
		serv_update = 1
	EndIf
Else	
	Color 180,0,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2,300,120,1
	Color 210,0,0
	Rect GraphicsWidth()/2-150,GraphicsHeight()/2,300,60,1
EndIf

Color 255,255,255
SetFont fontmid
Text GraphicsWidth()/2,FontHeight()*2,TEXT_OUTPUT$(1),1

Color 0,0,0
SetFont fontbig
Text GraphicsWidth()/2,GraphicsHeight()/2-GraphicsHeight()/4+60,TEXT_OUTPUT$(2),1,1
SetFont fontsmall
Text GraphicsWidth()/2,GraphicsHeight()/2+60,TEXT_OUTPUT$(3),1,1



Flip 0
If serv_update = True Then
	GetHighscore()
	Exit
EndIf

Forever
Delete Each star

EndIf
For k = 1 To 50
	s.star = New star
	s\sx = Rand(1,GraphicsWidth())
	s\sy = Rand(1,GraphicsHeight())
Next


FlushKeys()
FlushMouse

Repeat
WaitTimer ftimer
Cls
If win_act = False
	Color 255,255,255
Else
	Color 200,200,200
EndIf
For s2.star = Each star
	Plot s2\sx,s2\sy
Next
If win_act = False
	Color 100,180,100
Else
	Color 50,130,50
EndIf
SetFont fontmenu

;start game button
If MouseX() > GraphicsWidth()/2+100-StringWidth(TEXT_OUTPUT$(10))/2 And MouseX() < GraphicsWidth()/2+100 + StringWidth(TEXT_OUTPUT$(10))/2 And MouseY() > GraphicsWidth()/4+FontHeight() And MouseY() < GraphicsWidth()/4 + FontHeight()*2 And win_act = False 
	SetFont fontmenu_ch
	If MouseHit(1) Then
		MainLoop()
	EndIf
Else
	SetFont fontmenu
EndIf
Text GraphicsWidth()/2+100,GraphicsHeight()/4,TEXT_OUTPUT$(10),1,1

;highscore button
If MouseX() > GraphicsWidth()/2-100-StringWidth(TEXT_OUTPUT$(11))/2 And MouseX() < GraphicsWidth()/2-100+StringWidth(TEXT_OUTPUT$(11))/2 And MouseY() > GraphicsWidth()/3+40+FontHeight() And MouseY() < GraphicsWidth()/3+45 + FontHeight()*2 And win_act = False
	SetFont fontmenu_ch
	If MouseHit(1) Then
		HighscoreMenu()
	EndIf
Else
	SetFont fontmenu2
EndIf
Text GraphicsWidth()/2-100,GraphicsHeight()/3+25,TEXT_OUTPUT$(11),1,1

;options button
If MouseX() > GraphicsWidth()/2+50-StringWidth(TEXT_OUTPUT$(12))/2 And MouseX() < GraphicsWidth()/2+50+StringWidth(TEXT_OUTPUT$(12))/2 And MouseY() > GraphicsWidth()/2+FontHeight()*2 And MouseY() < GraphicsWidth()/2+FontHeight()*3+20 And win_act = False
	SetFont fontmenu_ch
	If MouseHit(1) Then
		OptionMenu()
	EndIf
Else
	SetFont fontmenu2
EndIf
Text GraphicsWidth()/2+50,GraphicsHeight()/2,TEXT_OUTPUT$(12),1,1

;exit button
If MouseX() > GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(13))/2 And MouseX() < GraphicsWidth()/2+StringWidth(TEXT_OUTPUT$(13))/2 And MouseY() > GraphicsWidth()/2+150+FontHeight()*2 And MouseY() < GraphicsWidth()/2+170+FontHeight()*3 And win_act = False
	SetFont fontmenu_ch
	If MouseHit(1) Then
		win_act = True
	EndIf
Else
	SetFont fontmenu2
EndIf
Text GraphicsWidth()/2,GraphicsHeight()/2+150,TEXT_OUTPUT$(13),1,1
	
If win_act = False
	Color 0,150,250
Else
	Color 0,100,200
EndIf
SetFont fontvers1
Text GraphicsWidth()-90,GraphicsHeight()-40,"V.",0,1
SetFont fontvers2
Text GraphicsWidth()-70,GraphicsHeight()-40,game_version$,0,1

If KeyHit(KEY_ESC) Or MouseHit(2) Then
	win_act = Not win_act
	FlushKeys()
	FlushMouse()
EndIf
If win_act = True Then			;asks if we're sure we wanna close the game
	SetFont fontmenu
	Color 50,50,50
	Rect GraphicsWidth()/2-100,GraphicsHeight()/2-FontHeight()*3,200,FontHeight()*3,1
	Color 255,255,255
	Rect GraphicsWidth()/2-100,GraphicsHeight()/2-FontHeight()*3,200,FontHeight()*3,0
	Color 0,200,50
	If MouseX() > GraphicsWidth()/2 And MouseX() < GraphicsWidth()/2+StringWidth(TEXT_OUTPUT$(5))+20 And MouseY() > GraphicsHeight()/2-FontHeight()*2+20 And MouseY() < GraphicsHeight()/2-20
		SetFont fontmenu_ch
		If MouseHit(1) Then
			FlushKeys()
			FlushMouse()
			win_act = False
		EndIf
	Else
		SetFont fontmenu2
	EndIf
	Text GraphicsWidth()/2+40,GraphicsHeight()/2-40,TEXT_OUTPUT$(5),1,1
	Color 200,0,0
	SetFont fontsmall
	If MouseX() > GraphicsWidth()/2-50-StringWidth(TEXT_OUTPUT$(4)) And MouseX() < GraphicsWidth()/2-35 And MouseY() > GraphicsHeight()/2-FontHeight()*2-10 And MouseY() < GraphicsHeight()/2-20
		SetFont fontsmall_ch
		If MouseHit(1) Then
			End
		EndIf
	Else
		SetFont fontsmall
	EndIf
	Text GraphicsWidth()/2-50,GraphicsHeight()/2-40,TEXT_OUTPUT$(4),1,1
	
	SetFont fontmid
	Color 0,200,250
	Text GraphicsWidth()/2,GraphicsHeight()/2-100,TEXT_OUTPUT$(18),1
	FlushMouse()
EndIf

SetFont fontsmall_k
Color 200,0,0
Text 20,GraphicsHeight()-FontHeight()*2,"Copyright by DGE"		;kek

If KeyHit(63) Then
	serv_update = True
	GetHighscore()
EndIf

Color 200,200,200
SetFont fontmessage1
For i = 1 To Len(server_message$)								;it even displayed server messages?
	Text SERVER_MSG_X(i),50,SERVER_MSG$(i)
	SERVER_MSG_X(i) = SERVER_MSG_X(i) - 1
	If SERVER_MSG_X(i) < 30 And SERVER_MSG_X(i) > 0 Then
		SERVER_MSG_X(i) = -30
	EndIf
	If SERVER_MSG_X(i) < -StringWidth(server_message$)-500-(Len(server_message$)*20) Then
		SERVER_MSG_X(i) = GraphicsWidth()-30
	EndIf
Next

Flip 0
FlushKeys()
FlushMouse()
Forever

End Function




Function OptionMenu()
For k = 1 To 50
	s.star = New star
	s\sx = Rand(1,GraphicsWidth())
	s\sy = Rand(1,GraphicsHeight())
Next
FlushKeys()
FlushMouse()
Repeat
Cls
DrawLevel(2)

Color 50,50,50
Rect GraphicsWidth()/7-25,GraphicsHeight()/4+80,GraphicsHeight()/2+40,FontHeight()*8,1
Color 255,255,255
Rect GraphicsWidth()/7-25,GraphicsHeight()/4+80,GraphicsHeight()/2+40,FontHeight()*8,0

Color 200,200,100
SetFont fontmenu_un
Text GraphicsWidth()/6,GraphicsHeight()/4,TEXT_OUTPUT$(14)
SetFont fontmid_nk
Color 160,100,0
If MouseX() > GraphicsWidth()/4-StringWidth(TEXT_OUTPUT$(17))/2 And MouseX() < GraphicsWidth()/4+StringWidth(TEXT_OUTPUT$(17))/2 And MouseY() > GraphicsHeight()/2+45 And MouseY() < GraphicsHeight()/2+FontHeight()+55
	SetFont fontmid_nk_un
	If MouseHit(1) Then
		graphic_level = 1
	EndIf
Else
	SetFont fontmid_nk
EndIf
Text GraphicsWidth()/4,GraphicsHeight()/2+50,TEXT_OUTPUT$(17),1

Color 220,180,0
If MouseX() > GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(15))/2 And MouseX() < GraphicsWidth()/2+StringWidth(TEXT_OUTPUT$(15))/2 And MouseY() > GraphicsHeight()/3+45 And MouseY() < GraphicsHeight()/3+FontHeight()+55
	SetFont fontmid_nk_un
	If MouseHit(1) Then
		graphic_level = 4
	EndIf
Else
	SetFont fontmid_nk
EndIf
Text GraphicsWidth()/2,GraphicsHeight()/3+50,TEXT_OUTPUT$(15),1

Color 150,150,150
If MouseX() > GraphicsWidth()*3/4-StringWidth(TEXT_OUTPUT$(16))/2 And MouseX() < GraphicsWidth()*3/4+StringWidth(TEXT_OUTPUT$(16))/2 And MouseY() > GraphicsHeight()/3+85 And MouseY() < GraphicsHeight()/3+FontHeight()+95
	SetFont fontmid_nk_un
	If MouseHit(1) Then
		graphic_level = 3
	EndIf
Else
	SetFont fontmid_nk
EndIf
Text GraphicsWidth()*3/4,GraphicsHeight()/3+90,TEXT_OUTPUT$(16),1

Select graphic_level
	Case 1
		Rect GraphicsWidth()/4-StringWidth(TEXT_OUTPUT$(17))/2-10,GraphicsHeight()/2+40,StringWidth(TEXT_OUTPUT$(17))+20,FontHeight()+20,0
	Case 3
		Rect GraphicsWidth()*3/4-StringWidth(TEXT_OUTPUT$(16))/2-10,GraphicsHeight()/3+80,StringWidth(TEXT_OUTPUT$(16))+20,FontHeight()+20,0
	Case 4
		Rect GraphicsWidth()/2-StringWidth(TEXT_OUTPUT$(15))/2-10,GraphicsHeight()/3+40,StringWidth(TEXT_OUTPUT$(15))+20,FontHeight()+20,0
End Select

If KeyHit(KEY_ESC) Or MouseHit(2) Then
	nphs2 = WriteFile("options")
	WriteInt nphs2,graphic_level
	CloseFile nphs2
	MainMenu()
EndIf

Flip 0
Forever
End Function




Function HighscoreMenu()
For k = 1 To 50
	s.star = New star
	s\sx = Rand(1,GraphicsWidth())
	s\sy = Rand(1,GraphicsHeight())
Next
xpl = 0

offset# = 0
max_range = 18

Repeat
Cls
mx = MouseX()
my = MouseY()

DrawLevel(2)
If serv_update = 1 Or True Then				;quick test fix for local highscore
	Color 50,50,50
	Rect 10,5,GraphicsWidth()-20,GraphicsHeight()-10,1
	Color 255,255,255
	Rect 10,5,GraphicsWidth()-20,GraphicsHeight()-10,0
	
	SetFont fontsmall
	
	For i = 1 To 60
		If i < 31 Then
			xn = 20
			xpl = GraphicsWidth()/4
			ypl = FontHeight()*(i - 1)
		Else
			xn = GraphicsWidth()/2 + 10
			xpl = GraphicsWidth()*3/4
			ypl = FontHeight()*(i - 31)
		EndIf
		
		
		Color 0,100,250
		Text xn,ypl+10,i+"."
		
		Color 0,150,50
		If Len(highscore_line$(i)) > max_range
			range = offset + max_range
			temp_string$ = highscore_line$(i) + "               "
			display_string$ = ""
			For j = Int(offset) To range
				display_string$ = display_string$ + Mid$(temp_string$, (j Mod Len(temp_string$)) + 1, 1)
			Next
			Text xn + 25,ypl+10,display_string$
		Else
			Text xpl + 25,ypl+10,highscore_line$(i),1
		EndIf
		
		If RectsOverlap(mx,my,1,1,xn,ypl+10,GraphicsWidth()/2,FontHeight()) Then
			InfoBox(xpl,ypl,highscore_date$(i),1)
		EndIf
	Next
Else
	SetFont fontmid_nk
	Text GraphicsWidth()/2,GraphicsHeight()/2,TEXT_OUTPUT$(19),1,1
EndIf
If KeyHit(KEY_ESC) Or MouseHit(2) Then
	Delete Each star
	MainMenu()
EndIf

Flip 0
offset = (offset + 0.07)
Forever
End Function




Function Min(a, b)
	If a > b
		Return b
	Else 
		Return a
	EndIf
End Function





Function CreateLevel(y=0)
r = 12
star_max = 20
If y = 1 Then
	extr_y = GraphicsHeight()
	r = Rand(4,4)
EndIf
If y = 2 Then star_max = 3
For k = 1 To star_max						;create stars in the background
	s.star = New star
	If y = 2 Then
		s\sx = Rand(0,GraphicsWidth())
		s\sy = Rand(0+scroll_y+extr_y*2,GraphicsHeight()+scroll_y+extr_y*2)
	Else
		s\sx = Rand(0,GraphicsWidth())
		s\sy = Rand(0+scroll_y-extr_y,GraphicsHeight()+scroll_y-extr_y)
	EndIf
Next
If y < 2 Then

;create and place bars
For i = 1 To r								
	If fb = True Then fbkind = 0						;the first bar (fb) is fixed
	cbx = Rand(0,GraphicsWidth()-bar_size)				;current bar x-pos
	If i = 1 Then
		cby = scroll_y-GraphicsHeight()-100				;current bar y-pos
		nb = True
	Else	
		cby = Rand(0 + scroll_y - extr_y,GraphicsHeight() + scroll_y - extr_y)
	EndIf
	If fb = False
		cby = scroll_y - 20
	EndIf
	For c2.bar = Each bar								;do not allow spawned bars to overlap
		col = True
		Repeat
			If RectsOverlap(cbx,cby,bar_size,40,c2\bx,c2\by,bar_size,40) Then
				cbx = Rand(0,GraphicsWidth()-bar_size)
				cby = Rand(0 + scroll_y - extr_y,GraphicsHeight() + scroll_y - extr_y)
			Else
				col = False
			EndIf
		Until col = False	
	Next
	c.bar = New bar										;creates a bar
	c\bx = cbx
	c\by = cby
	c\box = c\bx
	c\boy = c\by
	c\btime = MilliSecs() + 2000
	Select level_added									;set max rand value to roll for different bar types
		Case 1
			rmax = 20
		Case 2
			rmax = 60
		Case 3
			rmax = 70
		Case 4
			rmax = 80
		Case 5
			rmax = 100
		Case 7
			rmax = 120
		Case 9
			rmax = 140
		Case 11
			rmax = 160
		Case 13
			rmax = 180
		Default
			rmax = 200
	End Select
	
	
	If level_added < 14 Then							;roll the bar type
		ckr = Rand(1,rmax)
		If ckr < 21 Then
			If level_added > 8 Then
				cbkind = 7
			Else
				cbkind = 0
			EndIf
		ElseIf ckr > 50 And ckr < 61 Then
			cbkind = 1
			If rmax >= 70 Then
				If Rand(1,3) = 1 Then
					cbkind = 5
				EndIf
			EndIf
		ElseIf ckr > 60 And ckr < 65 Then
			cbkind = 2
			If ckr = 64 Then
				cbkind = 6
			EndIf
		ElseIf ckr > 70 And ckr < 72 Then
			cbkind = 3
			rr = Rand(1,6)
			If rr = 1 Then
				cbkind = 6
			EndIf
			If rr > 3 Then
				cbkind = 5
			EndIf
		ElseIf ckr > 71 And ckr < 80 Then
			If level_added < 7 Then
				cbkind = 4
			Else
				cbkind = 7
			EndIf
		ElseIf ckr > 90 And ckr < 121 Then
			cbkind = 7
		ElseIf ckr > 120 And ckr < 141 Then
			cbkind = 8
			If Rand(1,2) = 2 Then
				cbkind = 7
			EndIf
		ElseIf ckr > 140 And ckr < 161 Then
			cbkind = Rand(13,14)
		Else
			cbkind = 1
			rr = Rand(1,4)
			If rr > 1 And rr < 4 Then
				cbkind = 1
			EndIf
			If rr = 4 Then
				cbkind = 0
				If level_added > 9 Then
					cbkind = 7
				EndIf
			EndIf
			If level_added > 6 And level_added < 11 Then
				If rr = 1 Then
					cbkind = 7
				ElseIf rr > 2 Then
					cbkind = 8
				EndIf
			ElseIf level_added > 10 Then
				If rr = 1 Or rr = 2 Then
					cbkind = 15
				EndIf
			Else
				If rr = 1 Then
					cbkind = 4
				EndIf
			EndIf
		EndIf
	Else
		ckr = Rand(1,111)
		If ckr < 51 Then
			cbkind = 15
		ElseIf ckr > 50 And ckr < 101 Then
			rr = Rand(1,3)
			cbkind = 15
			If rr = 1 Then
				cbkind = 1
			EndIf
			If rr = 3 Then
				cbkind = 5
			EndIf
		ElseIf ckr > 100 And ckr < 110 Then
			rr = Rand(1,2)
			If rr = 1 Then
				cbkind = 15
			Else
				cbkind = 3
			EndIf
		Else
			cbkind = 8
			rr = Rand(1,10)
			If rr < 4 Then
				cbkind = 4
			EndIf
			If rr = 10 Then
				cbkind = 15
			EndIf
		EndIf
	EndIf
	c\bkind = cbkind
	If nb = True Then
		nb = False
	EndIf
	fb = True
Next
EndIf
End Function




Function DrawLevel(y=0)
;oh, this one's cool, you get to see people from the highscore list you're currently surpassing
For i = 1 To 20				
	SetFont fontsmall_ch
	If win_ack = False
		Color 200,0,200
	Else
		Color 150,0,150
	EndIf
	If highscore_name$(i) <> "EMPTY" And highscore_name$(i) <> "" Then
		If y <> 2 Then
			Text GraphicsWidth()-StringWidth(Str(i)+"."+highscore_name$(i)+"  "),-highscore_score(i)-scroll_y+GraphicsHeight()/2,i+"."+highscore_name$(i)
		EndIf
	EndIf
Next
SetFont fontsmall

;draw stars
Color 200,200,200
If graphic_level >= 3 Then
	If pl_fall = False
		LockBuffer BackBuffer()
		For s.star = Each star
			WritePixel s\sx,s\sy - scroll_y,$FFFFFF
			If s\sy - scroll_y > GraphicsHeight()*2 Then Delete s
		Next
		UnlockBuffer BackBuffer()
	Else
		For s.star = Each star
			Color 255,255,255
			Plot s\sx,s\sy - scroll_y
			If s\sy - scroll_y > GraphicsHeight()*2 Then Delete s
		Next
	EndIf
EndIf

;draw bars
For c.bar = Each bar
	If graphic_level < 4
		If win_act = False
			Color 0,250,250
			If c\bkind = 1 Then Color 250,250,0
			If c\bkind = 2 Then Color 0,250,0
			If c\bkind = 3 Then Color 250,0,0
			If c\bkind = 4 Then Color 50,150,250
			If c\bkind = 5 Then Color 200,200,200
			If c\bkind = 6 Then Color 250,0,250
			If c\bkind = 7 Then Color 255,255,255
		Else
			Color 0,200,200
			If c\bkind = 1 Then Color 200,200,0
			If c\bkind = 2 Then Color 0,200,0
			If c\bkind = 3 Then Color 200,0,0
			If c\bkind = 4 Then Color 0,100,200
			If c\bkind = 5 Then Color 150,150,150
			If c\bkind = 6 Then Color 200,0,200
			If c\bkind = 7 Then Color 200,200,200
		EndIf
		If graphic_level > 1 Then
			Rect c\bx-1,c\by - scroll_y-1,bar_size+2,7,1
		EndIf
		If win_act = False
			Color 0,200,200
			If c\bkind = 1 Then Color 200,200,0
			If c\bkind = 2 Then Color 0,200,0
			If c\bkind = 3 Then Color 200,0,0
			If c\bkind = 4 Then Color 50,100,200
			If c\bkind = 5 Then Color 150,150,150
			If c\bkind = 6 Then Color 200,0,200
			If c\bkind = 7 Then Color 0,0,0
		Else
			Color 0,150,150
			If c\bkind = 1 Then Color 150,150,0
			If c\bkind = 2 Then Color 0,150,0
			If c\bkind = 3 Then Color 150,0,0
			If c\bkind = 4 Then Color 0,50,150
			If c\bkind = 5 Then Color 100,100,100
			If c\bkind = 6 Then Color 150,0,150
			If c\bkind = 7 Then Color 0,0,0
		EndIf
		Rect c\bx,c\by - scroll_y,bar_size,5,1
		
		If c\bkind = 7 Then
			If win_act = False
				Color 255,255,255
			Else
				Color 200,200,200
			EndIf
			Rect c\bx,c\by - scroll_y,bar_size,5,0
		EndIf
		If c\by - scroll_y > GraphicsHeight() Then Delete c
	Else
		DrawImage bar_image,c\bx,c\by-scroll_y,c\bkind+c\bframe
		If c\bkind = 8 And c\btime < MilliSecs()
			c\bframe = (MilliSecs() - c\btime)/150 Mod 6
			If c\bframe > 4 Then
				Delete c
			EndIf
		EndIf
	EndIf
Next

If player_status = 2		;effect that makes you see only a fraction of the screen surrounding the player
	If (player_y - scroll_y) < GraphicsHeight()
		DrawImage fow,player_x,player_y-scroll_y-20
	Else
		player_status_time = 0
	EndIf
EndIf

If y = 0
	If win_act = False
		If graphic_level < 4 Then
			Color 255,0,0
			Rect player_x#-player_size/2,player_y# - scroll_y,player_size,5
		Else
			DrawImage player_image,player_x#-player_size/2,player_y#-scroll_y-40,player_condition
		EndIf
		
		Color 50,50,50
		Rect -1,-1,StringWidth("Punkte: 2.000.000"),FontHeight()*3,1
		Color 255,255,255
		Rect -1,-1,StringWidth("Punkte: 2.000.000"),FontHeight()*3,0
		
		Color 200,50,200
		If pl_fall = False
			Text 10,10,TEXT_OUTPUT$(7)+Abs(scroll_y)
		Else
			Text 10,10,TEXT_OUTPUT$(7)+Abs(scroll_y_f)
		EndIf
		Text 10,25,TEXT_OUTPUT$(20)+player_sp_jump
	Else
		If graphic_level < 4 Then
			Color 200,0,0
			Rect player_x#-player_size/2,player_y# - scroll_y,player_size,5
		Else
			DrawImage player_image,player_x#-player_size/2,player_y#-scroll_y-40,player_condition
		EndIf
		
		Color 40,40,40
		Rect -1,-1,StringWidth("Punkte: 2.000.000"),FontHeight()*3,1
		Color 200,200,200
		Rect -1,-1,StringWidth("Punkte: 2.000.000"),FontHeight()*3,0
		
		Color 150,0,150
		If pl_fall = False
			Text 10,10,TEXT_OUTPUT$(7)+Abs(scroll_y)
		Else
			Text 10,10,TEXT_OUTPUT$(7)+Abs(scroll_y_f)
		EndIf
		Text 10,25,TEXT_OUTPUT$(20)+player_sp_jump
	EndIf
EndIf

;something...
If player_acc# > 15 And plshadow_cd < MilliSecs() And graphic_level = 3 Then
	plshadow_cd = MilliSecs() + 50
	s3.shadow = New shadow
	s3\px = player_x
	s3\py = player_y
	s3\ptime = plshadow_cd
EndIf
For s2.shadow = Each shadow
	Color 200,0,0
	Rect s2\px,s2\py - scroll_y,player_size,5
	If s2\py - scroll_y > GraphicsHeight() Then Delete s2
	Color 255,0,0
	Rect player_x#,player_y# - scroll_y,player_size,5
Next

;draw that silly landing effect when dropping on a bar
If graphic_level >= 4
	For c3.quake = Each quake
		Color c3\qr,c3\qg,c3\qb
		Plot c3\qx-c3\qbx,c3\qy-scroll_y
		Plot c3\qx+c3\qbx,c3\qy-scroll_y
		c3\qr = c3\qr - 5
		c3\qbx = c3\qbx + 2
		If c3\qr < 0
			Delete c3
		EndIf
	Next
EndIf
If player_status_time > MilliSecs() Then
	pst# = (player_status_time-MilliSecs())
	Color 200,100,100
	Text GraphicsWidth()/2,200,pst#/1000,1
EndIf
End Function




Function MovePlayer()
If player_moved = True 															;makes player fall
	player_acc# = player_acc# - gravity#
	player_y# = player_y# - player_acc#
EndIf
If KeyHit(KEY_SPACE) And player_sp_jump > 0 And pl_fall = False Then			;use super jump
	player_moved = True
	player_acc# = 8
	player_sp_jump = player_sp_jump - 1
EndIf

If KeyHit(KEY_LEFT) Or KeyHit(KEY_RIGHT) Or KeyHit(KEY_A) Or KeyHit(KEY_D)		;I guess this was used to start the game (i.e. the player sits in air until moved)
	player_moved = True
EndIf

If KeyDown(KEY_LEFT) Or KeyDown(KEY_A) Then										;move left
	If player_status = 1 Then
		player_x# = player_x# + player_speed#
	Else																		;invert the directions if player under effect of certain bar he touched
		player_x# = player_x# - player_speed#
	EndIf
	If player_acc# > 0 Then
		player_condition = 2													;eye position (the player sprite looks in the moving direction)
	EndIf
EndIf
If KeyDown(KEY_RIGHT) Or KeyDown(KEY_D) Then									;move right
	If player_status = 1 Then
		player_x# = player_x# - player_speed#
	Else
		player_x# = player_x# + player_speed#
	EndIf
	If player_acc# > 0 Then
		player_condition = 3
	EndIf
Else
	player_condition = 0
EndIf
If player_x# < 0 - player_size Then												;if player goes too far left, he spawns on the right and vice versa
	player_x# = GraphicsWidth() + player_size
EndIf
If player_x# > GraphicsWidth() + player_size Then
	player_x# = 0 - player_size
EndIf


If player_acc# < -0.5 Then
	player_condition = 1
EndIf
If player_status_time < MilliSecs() Then										;remove the status
	player_status = 0
EndIf
For c2.bar = Each bar
	;bar collisions and a lot of different bar effects/updates
	If RectsOverlap(player_x#-player_size/2+2,player_y#,player_size-4,5,c2\bx,c2\by,bar_size,10) And player_acc# < 0
		If c2\bkind = 15 Then
			rr = Rand(1,6)
			Select rr
				Case 1
					c2\bkind = 1
				Case 2
					c2\bkind = 7
				Case 3
					c2\bkind = 14
				Case 4
					c2\bkind = 13
				Case 5
					c2\bkind = 4
				Case 6
					c2\bkind = 8
			End Select
		EndIf
		If c2\bkind = 2 Then extr_jump = 8
		If c2\bkind = 3 Then extr_jump = 20
		If c2\bkind = 6 Then
			extr_jump = 30
		EndIf
		If c2\bkind = 4 Then extr_jump = Rand(-3,3)
		If c2\bkind = 7 Then dltbar = True
		If c2\bkind = 13 And player_status = 0 Then
			player_status = 1
			player_status_time = MilliSecs()+4000					;some magic numbers
		EndIf
		If c2\bkind = 14 Then
			player_status_time = MilliSecs()+10000
			player_status = 2
		EndIf
		player_acc# = 5.5 + Abs(player_acc#/5) + extr_jump
		If graphic_level >= 4 Then
			For i = 1 To 20 Step 1
				d.quake = New quake
				d\qx = player_x-i+10
				d\qy = player_y
				d\qr = 250
				d\qg = 50
				d\qb = 50
				d\qtime = MilliSecs() + 200
			Next
		EndIf
	EndIf
	If c2\bkind = 1 Then
		If (c2\bx > c2\box - GraphicsWidth()/4 And c2\bmove = 0) And c2\bx > 0 Then
			c2\bx = c2\bx - 1
		Else
			c2\bx = c2\bx + 1
			c2\bmove = 1
			If c2\bx > c2\box + GraphicsWidth()/4 Or c2\bx > GraphicsWidth() - bar_size Then
				c2\bmove = 0
			EndIf
		EndIf
	EndIf
	If c2\bkind = 5 Then
		If (c2\by > c2\boy - GraphicsHeight()/8 And c2\bmove = 0); Or c2\by  < -scroll_y Then
			c2\by = c2\by - 1
		Else
			c2\by = c2\by + 1
			c2\bmove = 1
			If c2\by > c2\boy + GraphicsHeight()/8 ;Or c2\by > GraphicsHeight() - 5 - scroll_y Then
				c2\bmove = 0
			EndIf
		EndIf
	EndIf
	If dltbar = True Or c2\by > GraphicsHeight()+scroll_y
		Delete c2
		dltbar = False
	EndIf
Next
If player_y# < GraphicsHeight()/2 + scroll_y Then				;scroll screen if player gets in the upper half of the screen
	If player_acc > 0
		scroll_y = scroll_y - player_acc
	EndIf
EndIf
If player_y# < -scroll_y And level_p = False Then
	level_p = True
	level_line = scroll_y
	CreateLevel(1)
EndIf
If level_p = True And player_y# < level_line
	level_p = False
EndIf
If Abs(scroll_y) > 17500*player_sp_jump_added Then				;give the player superjumps once in a while (a modulo would've worked as well..)
	player_sp_jump_added = player_sp_jump_added + 1
	player_sp_jump = player_sp_jump + 1
EndIf
If Abs(scroll_y) > 9500*level_added Then						;same stuff with levels
	level_added = level_added + 1
EndIf
If player_y - scroll_y > GraphicsHeight()*3 Then				;not sure what/why that is
	CreateLevel(2)
	If pl_fall = False
		scroll_y_f = Abs(scroll_y)
		scroll_y = scroll_y + 500
		pl_fall = True
		If player_score < scroll_y_f Then
			nphs = WriteFile("highscore")
			WriteInt nphs,scroll_y_f
			WriteString nphs,game_version$
			WriteString nphs,Mid(CurrentTime(),1,5) + " / " + CurrentDate()
			CloseFile nphs
			player_score = scroll_y_f
		EndIf
	EndIf
	scroll_y = scroll_y - player_acc
	If KeyHit(KEY_ENTER) Then
		game_over = True
		FlushKeys()
		Delete Each star
	EndIf
	If scroll_y > 0 Then
		scroll_y = 0
		game_over = True
		Delete Each star
	EndIf
EndIf

If KeyHit(KEY_1) Then											;take a screenshot and save it
	If cur_scr_num < 10
		scr_suf$ = Str$("00"+cur_scr_num)
	ElseIf cur_scr_num < 100
		scr_suf$ = Str$("0"+cur_scr_num)
	Else
		scr_suf$ = Str$(cur_scr_num)
	EndIf
	SaveBuffer(FrontBuffer(),"screenshot_"+scr_suf$+".bmp")
	cur_scr_num = cur_scr_num + 1
EndIf
End Function



Function ResetAll()
Delete Each bar
Delete Each star
Delete Each shadow
gravity# = 0.075

player_x#=GraphicsWidth()/2
player_y#=GraphicsHeight()/4
player_speed# = 4.2
player_jump = 1
player_acc# = 0
level_line = 0
level_p = 0
player_sp_jump = 1
player_sp_jump_added = 1
player_moved = False

paused = False
level_added = 1
server_ip$ = ""
pl_fall = False
If graphic_level < 4 Then
	player_size = 15
Else
	player_size = 45
EndIf

scroll_y = 0
scroll_y_f = 0

;player_score = 0
FlushKeys()
End Function



Function GetPlayerName()
	nfile = ReadFile("playername")
	If nfile = 0 Then
		Repeat												;waits for valid name input
			nname$ = Input(TEXT_OUTPUT$(21))
			If nname$ = "" Or Instr(nname$,"(") <> 0 Then
				Print TEXT_OUTPUT$(22)
			EndIf
		Until nname$ <> ""
		nnf = WriteFile("playername")
		WriteString nnf,nname$
		player_name$ = nname$
		ExecFile "SiskoJump.exe"							;restart game
		
		If Lower$(player_name$) = "wabosh" Then RuntimeError TEXT_OUTPUT$(25)
		End
	Else
		player_name$ = ReadString(nfile)
	EndIf
End Function




Function GetHighscore()
Cls
SetFont fontmessage1
Color 255,255,255
Text 200,200,"Upload...",1
Flip
t = CreateTimer(220)
Locate 0,0

If serv_update = 1 Then
	GetPlayerName()
	ipstrm = OpenTCPStream("s-w-u.dyndns.org",12346)		;old server name :'), it's used to grab the server ip, not sure what the original motivation was tho
															;maybe somithing like grabbing ips/ports for different servers for different games, idk
	If ipstrm = 0 Then
		RuntimeError TEXT_OUTPUT$(23)
		serv_update = 0
	EndIf
	Repeat
	WaitTimer t
	WriteString ipstrm,player_name$ + " - " + player_user_name$ + "(" + game_version + ")"
	While ReadAvail(ipstrm)
		server_ip$ = ReadString(ipstrm)
		If Instr(server_ip$,"||UPDATE_TEXT||") <> 0 Then				;some mumbo jumbo with command types of server messages
			server_ip$ = Replace(server_ip$,"||UPDATE_TEXT||","")
			nv = WriteFile("update_link.txt")
			WriteLine nv,server_ip$
			RuntimeError server_ip$
		EndIf
	Wend
	Until server_ip$ <> ""
	If Instr(server_ip$,"||SERVER_MSG||") = 0 Then RuntimeError "pnf oda holt euch einfach die neue Version!"
	server_message$ = Right(server_ip$,Len(server_ip$)-Instr(server_ip$,"||SERVER_MSG||")-13)
	server_ip$ = Left(server_ip$,Instr(server_ip$,"||SERVER_MSG||")-1)
	For i = 1 To Len(server_message$)
		SERVER_MSG$(i) = Mid(server_message$,i,1)
		SERVER_MSG_X(i) = 300+(20)*i
	Next
	CloseTCPStream ipstrm
EndIf

If player_score = 0
	hsf = ReadFile("highscore")
	If hsf = 0 Then
		hsf = WriteFile("highscore")
		WriteInt hsf,0
		WriteString hsf,game_version$
		WriteString hsf,Mid(CurrentTime(),1,5) + " / " + CurrentDate()
		CloseFile hsf
		hsf = OpenFile("highscore")
	EndIf
	
	player_score = ReadInt(hsf)
	gmv$ = ReadString(hsf)
	hsdate$ = ReadString(hsf)
	If hsdate$ = "" Then
		hsdate$ = "EMPTY"
	EndIf
	CloseFile hsf
EndIf

If player_score = 0
	player_score = scroll_y_f
EndIf

If graphic_level = 0 Then
	graphic_level = 2
EndIf


For l = 1 To 60
	highscore_line$(l) = ""
Next

If serv_update = 1 Then							;grabs the highscore from the server
	hsstream = OpenTCPStream(server_ip$,12345)
	If hsstream = 0 Then RuntimeError TEXT_OUTPUT$(24)
	SetFont fontsmall
	Repeat
	WaitTimer t
	For i = 1 To 60
		If hsdate$ = "" Then hsdate$ = Mid(CurrentTime(),1,5) + " / " + CurrentDate()
		WriteString hsstream,player_name$ + "||$C0R3||" + player_score + "||DATE||" + hsdate$
	Next
	While ReadAvail(hsstream)
		For j = 1 To 60
			highscore_line$(j) = ReadString(hsstream)
			
			p1 = Instr(highscore_line$(j),"||$C0R3||")
			If p1 = 0 Then RuntimeError "p1"
			p2 = Instr(highscore_line$(j),"||DATE||")
			If p2 = 0 Then RuntimeError "p2: " + highscore_line$(j)
			highscore_score(j) = Int(Mid(highscore_line(j),p1+9,p2-p1-9))
			highscore_name$(j) = Left(highscore_line(j),Instr(highscore_line(j),"||$C0R3||")-1)
			highscore_date$(j) = Right(highscore_line$(j),Len(highscore_line(j))-p2-7)
			highscore_line$(j) = highscore_name$(j) + " - " + highscore_score(j)
		Next
	Wend
	Until highscore_line(60) <> ""
	CloseTCPStream hsstream
EndIf
End Function




Function GetLocalHighscore()
	For i = 1 To max_highscore_num
		highscore_score(i) = 0
		highscore_name$(i) = ""
		highscore_date$(i) = ""
	Next

	hsf = ReadFile("local_highscore")
	If Not hsf Then hsf = WriteFile("local_highscore")
	i = 0
	While Not eof(hsf)
		name$ = ReadString(hsf)
		score = ReadInt(hsf)
		date$ = ReadString(hsf)

		highscore_score(i) = score
		highscore_name$(i) = name$
		highscore_date$(i) = date$
		highscore_line$(i) = highscore_name$(i) + " - " + highscore_score(i)

		i = i + 1
	Wend
End Function


Function AddLocalHighscore(name$, score, date$)
	rank = 0
	For i = 1 To max_highscore_num
		If score >= highscore_score(i) Then
			rank = i
			Exit
		EndIf
	Next

	For i = max_highscore_num To rank + 1 Step -1
		highscore_score(i) = highscore_score(i - 1)
		highscore_name$(i) = highscore_name(i - 1)
		highscore_date$(i) = highscore_date$(i - 1)
		highscore_line$(i) = highscore_line$(i - 1)
	Next

	highscore_score(i) = score
	highscore_name$(i) = name$
	highscore_date$(i) = date$
	highscore_line$(i) = highscore_name$(i) + " - " + highscore_score(i)

	SaveLocalHighscore()
End Function



Function SetLocalHighscore(name$, score, date$)
	For i = 1 To max_highscore_num
		If highscore_name$(i) = name$ Then
			If highscore_score(i) < score Then
				highscore_score(i) = score
				highscore_name$(i) = name$
				highscore_date$(i) = date$
				highscore_line$(i) = highscore_name$(i) + " - " + highscore_score(i)

				SaveLocalHighscore()
			EndIf
			Return
		EndIf
	Next

	AddLocalHighscore(name$, score, date$)
End Function



Function SaveLocalHighscore()
	hsf = WriteFile("local_highscore")
	SortHighscores()
	
	For i = 0 To max_highscore_num
		WriteString hsf, highscore_name$(i)
		WriteInt hsf, highscore_score(i)
		WriteString hsf, highscore_date$(i)
	Next

	CloseFile hsf
End Function



Function SortHighscores()			;just bubble sort this
	For i = 1 To max_highscore_num
		For j = i To max_highscore_num
			If highscore_score(i) < highscore_score(j) Then
				temp_score = highscore_score(j)
				temp_name$ = highscore_name$(j)
				temp_date$ = highscore_date$(j)
				temp_line$ = highscore_line$(j)

				highscore_score(j) = highscore_score(i)
				highscore_name$(j) = highscore_name$(i)
				highscore_date$(j) = highscore_date$(i)
				highscore_line$(j) = highscore_line$(i)

				highscore_score(i) = temp_score
				highscore_name$(i) = temp_name$
				highscore_date$(i) = temp_date$
				highscore_line$(i) = temp_line$
			EndIf
		Next
	Next
End Function




Function BroadcastIP$()
	CountHostIPs("")
	Local IP$ = DottedIP(HostIP(1))
	Local Subnetmask$ = "255.255.255.0"
	Local IPDigits[3], SubnetDigits[3]
	Local pos, i
	For i = 0 To 3
		pos = Instr( IP$, "." )
		If pos > 0 Then
			IPDigits[i] = Left( IP$, pos -1 )
			IP$ = Mid( IP$, pos +1 )
		Else
			IPDigits[i] = IP$
		EndIf
	Next
	For i = 0 To 3
		pos = Instr( Subnetmask$, "." )
		If pos > 0 Then
			SubnetDigits[i] = Left( Subnetmask$, pos -1 ) Xor $FF
			Subnetmask$ = Mid( Subnetmask$, pos +1 )
		Else
			SubnetDigits[i] = Subnetmask$ Xor $FF
		EndIf
	Next
	For i = 0 To 3
		IPDigits[i] = IPDigits[i] Or SubnetDigits[i]
	Next		
	Return IPDigits[0] +"." +IPDigits[1] +"." +IPDigits[2] +"." +IPDigits[3]
End Function



Function InfoBox(x,y,txt$,loc=1)
Color 50,50,50
Rect x-(StringWidth(txt$)/2*loc),y,StringWidth(txt$),FontHeight(),1
Color 200,200,200
Rect x-(StringWidth(txt$)/2*loc),y,StringWidth(txt$),FontHeight(),0
Color 0,200,200
Text x,y,txt$,loc
End Function



Function FPS()
If current_sec < MilliSecs() Then
	current_sec = MilliSecs() + 1000
	fps_rate_old = fps_rate
	fps_rate = 0
Else
	fps_rate = fps_rate + 1
EndIf
Return fps_rate_old
End Function













;Some experimental code for "archived" images. This was supposed to extract images from a single file
;(or just store all the image info in a string or something)
;and thus remove the need of a whole assets folder. Was not supposed to reduce memory usage tho.


Function LoadAImage(name$,source$="",fsx=0,fsy=0,frame_start=0,frame_end=0)
If fsx > GraphicsWidth() Or fsy > GraphicsHeight() Then
	RuntimeError "Frame resolution is bigger than the graphics resolution!"
EndIf

For c.ArchiveImage = Each ArchiveImage
	If source$ = "" Then
		If c\AIName$ = name$ Then
			If fsx > 0 And fsy > 0
				SetBuffer FrontBuffer()
				For i = 0 To frame_end
					DrawImage c\AIImage,0-actl_x,0-actl_y
					c\AIFrame[i] = CreateImage(fsx,fsy)
					If actl_x + fsx > c\AIsx
						actl_x = 0
						actl_y = actl_y + fsy
						If actl_y > c\AIsy Then RuntimeError "Frame out of bounds!"
					EndIf
					GrabImage c\AIFrame[i],0,0
					actl_x = actl_x + fsx
					c\AIMaxFrames = i
				Next
			EndIf
			Return c\AIImage
		EndIf
	Else
		If c\AIName$ = name$ Then
			If c\AIAName$ = source$ Then
				If fsx > 0 And fsy > 0
					SetBuffer FrontBuffer()
					For i = 0 To frame_end
						DrawImage c\AIImage,0-actl_x,0-actl_y
						c\AIFrame[i] = CreateImage(fsx,fsy)
						If actl_x + fsx > c\AIsx
							actl_x = 0
							actl_y = actl_y + fsy
							If actl_y > c\AIsy Then RuntimeError "Frame out of bounds!"
						EndIf
						GrabImage c\AIFrame[i],0,0
						actl_x = actl_x + fsx
						c\AIMaxFrames = i
					Next
				EndIf
				Return c\AIImage
			EndIf
		EndIf
	EndIf
Next
RuntimeError "Image does not exist!"
End Function


Function InitAI(source$)
AIfile$ = ReadFile(source$)
If AIfile$ = 0 Then RuntimeError "Archive could not be found!"
Repeat
	Cls
	c.ArchiveImage = New ArchiveImage
	c\AIAName$ = source$
	c\AIsx = ReadShort(AIfile$)
	c\AIsy = ReadShort(AIfile$)
	c\AIImage = CreateImage(c\AIsx,c\AIsy)
	
	SetBuffer ImageBuffer(c\AIImage)
	For y = 0 To c\AIsy
		For x = 0 To c\AIsx
			cr = ReadByte(AIfile$)
			cg = ReadByte(AIfile$)
			cb = ReadByte(AIfile$)
			Color cr,cg,cb
			Plot x,y
		Next
	Next
	SetBuffer FrontBuffer()
	c\AIName$ = ReadString(AIfile$)
Until Eof(AIfile$)

SetBuffer FrontBuffer()
Color 255,255,255
End Function


Function DrawAnimImage(ID,x,y,frame)
For c.ArchiveImage = Each ArchiveImage
	If ID = c\AIImage Then
		If c\AIFrame[frame] <> 0
			DrawImage c\AIFrame[frame],x,y
		EndIf
	EndIf
Next
End Function


Function MaskAImage(ID,cr,cg,cb)
For c.ArchiveImage = Each ArchiveImage
	If ID = c\AIImage Then
		For i = 0 To c\AIMaxFrames
			If c\AIFrame[i] <> 0 Then
				MaskImage c\AIFrame[i],cr,cg,cb
			EndIf
		Next
	EndIf
Next
End Function




Function Loadbar(bnum)
SetFont font_small
For i = 1 To bnum
	Locate 20+i*10,800-FontHeight()-20
	Write "|"
Next
End Function