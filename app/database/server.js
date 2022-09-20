var express = require("express")
var app = express()
//const scheduler = require('./scheduler.js')
const db = require('better-sqlite3')('./app/database/database.db');

var HTTP_PORT = 8000

app.use(express.json())

app.listen(HTTP_PORT, () => {
    console.log("Server running on port %PORT%".replace("%PORT%",HTTP_PORT))
});

/* -----------------------------------
   ------------ SIGN IN --------------
   ----------------------------------- */
app.post("/login", (req, res, next) => {
    var username = req.body.username
    var password = req.body.password
    var query = "SELECT * from users WHERE username=? AND password=?;"

    const row = db.prepare('SELECT * from users WHERE username=? AND password=?;').get(username, password);

    if (row != null) {
        const send = {
            username: row.username,
            id: row.id
        }

         res.status(200).send(JSON.stringify(send))
    } else {
        res.status(404).send()
    }

    console.log(req.body)
});

/* -----------------------------------
   ------------ SIGN UP --------------
   ----------------------------------- */
app.post("/signup", (req, res, next) => {
    var username = req.body.username
    var email = req.body.email
    var password = req.body.password

    var send = {
        username: "valid",
        email: "valid"
    }

    var isValid = true;

    if(!validUsername(username, send)) {
        isValid = false
    }

    if(!validEmail(email, send)) {
        isValid = false
    }

    if(isValid) {
        createAccount(username, email, password)

        res.status(200).send(JSON.stringify(send))
    } else {
        console.log(send)
        res.status(201).send(JSON.stringify(send))
    }

    console.log(req.body)
});

function createAccount(username, email, password) {
    const row = db.prepare('INSERT INTO users(username, email, password) VALUES (?,?,?)')
    row.run(username, email, password)
}

function validUsername(username, send) {
    const row = db.prepare('SELECT * from users WHERE username=?;').get(username);

    if (row != null) {
        console.log("invalid username")
        send.username = "invalid"
        return false
    } else {
        console.log("valid username")
        return true
    }
}

function validEmail(email, send) {
    const row = db.prepare('SELECT * from users WHERE email=?;').get(email);

    if (row != null) {
        send.email = "invalid"
        return false
    } else {
        console.log("valid username")
        return true
    }
}

/* -----------------------------------
   ------------ Groups --------------
   ----------------------------------- */

app.post("/group", (req, res, next) => {
    var username = req.body.username

    var query = 'SELECT group_id FROM user_group WHERE user_id = (select id from users where username=?);'
    var group_id = db.prepare(query).all(username);

    if(group_id == null) {
        res.status(404).send()
        return
    }

    var result = {}
    var groups = "Groups"
    result[groups] = []

    for (i = 0; i < group_id.length; i++) {
        var name = db.prepare('SELECT name from groups WHERE id=?').get(group_id[i].group_id);

        const temp = {
            id: group_id[i].group_id,
            name: name.name
        }

        result[groups].push(temp)
    }

    const send = {
        groups: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ---------- Create group -----------
   ----------------------------------- */

app.post("/create_group", (req, res, next) => {
    var username = req.body.username
    var group_name = req.body.group_name
    var user_id = req.body.user_id
    var goal_value = req.body.goal_value
    var pass_value = req.body.pass_value
    var prize_value = req.body.prize_value

    var query = 'SELECT * from groups WHERE name=?;'
    var row = db.prepare(query).get(group_name);

    if (row == null) {
        var query = 'INSERT INTO groups (name, goal_value, pass_value, prize_value) VALUES (?, ?, ?, ?);'
        var row = db.prepare(query).run(group_name, goal_value, pass_value, prize_value);

        console.log(row.lastInsertRowid)

        var query = 'INSERT INTO user_group(user_id, group_id, admin_rights, has_confirmed_players) values (? , ?, 1, 0);'
        var row = db.prepare(query).run(user_id, row.lastInsertRowid)

        const send = {
            groups: "success"
        }

        res.status(200).send(JSON.stringify(send))
    } else {
        res.status(404).send()
    }
});

/* -----------------------------------
   ---------- Get all users ----------
   ----------------------------------- */

app.get("/all_users", (req, res, next) => {
    var query = 'SELECT username from users;'
    var users = db.prepare(query).all()

    var result = {}
    var users_key = "Users"
    result[users_key] = []

    for (i = 0; i < users.length; i++) {
        result[users_key].push(users[i].username)
    }

    const send = {
        users: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   -------- Group invitation ---------
   ----------------------------------- */

function user_already_invited(user_id, group_id) {
    var query = 'SELECT * from invitations_group WHERE user_id=? AND group_id=?;'
    var invitation = db.prepare(query).get(user_id, group_id)

    return invitation != null
}

function user_already_in_group(user_id, group_id) {
    var query = 'SELECT * from user_group WHERE user_id=? AND group_id=?;'
    var user_group = db.prepare(query).get(user_id, group_id)

    return user_group != null
}

app.post("/invite_group", (req, res, next) => {
    var username = req.body.username
    var group_id = req.body.groupId

    var query = 'SELECT id from users WHERE username=?;'
    var user_id = db.prepare(query).get(username)

    if(user_id == null) {
        res.status(404).send()
        return
    } else if(user_already_invited(user_id.id, group_id)) {
        const send = {
            users: "Already invited"
        }

        res.status(201).send(send)
        return
    } else if(user_already_in_group(user_id.id, group_id)) {
        const send = {
            users: "Already in group"
        }

        res.status(202).send(send)
        return
    }

    var query = 'SELECT name FROM groups WHERE id=?;'
    var group_name = db.prepare(query).get(group_id)
    console.log(group_name.name)

    var query = "insert into invitations_group(user_id, group_id, group_name) values (?, ?, ?);"
    var users = db.prepare(query).run(user_id.id, group_id, group_name.name)

    const send = {
        users: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   --------- Get invitations ---------
   ----------------------------------- */

app.post("/invitations", (req, res, next) => {
    var user_id = req.body.user_id

    var query = 'SELECT * from invitations_group where user_id=?;'
    var invitations = db.prepare(query).all(user_id)

    var result = {}
    var invitation_key = "Invitations"
    result[invitation_key] = []

    for (i = 0; i < invitations.length; i++) {
        result[invitation_key].push(invitations[i])
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   -------- Accept invitation --------
   ----------------------------------- */

app.post("/accept_invitation", (req, res, next) => {
    var invitation_id = req.body.invitation_id
    var group_id = req.body.group_id
    var user_id = req.body.user_id

    var query = 'insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values (?, ?, 0, 0);'
    var invitations = db.prepare(query).run(user_id, group_id)

    var query = 'DELETE FROM invitations_group WHERE id=?'
    var invitations = db.prepare(query).run(invitation_id)

    const send = {
        result: "Success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   -------- Decline invitation -------
   ----------------------------------- */

app.post("/decline_invitation", (req, res, next) => {
    var invitation_id = req.body.invitation_id

    var query = 'DELETE FROM invitations_group WHERE id=?'
    var invitations = db.prepare(query).run(invitation_id)

    const send = {
        result: "Success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ------- Get first 30 players ------
   ----------------------------------- */

app.get("/first_players", (req, res, next) => {
    var query = 'SELECT * from players LIMIT 30;'
    var players = db.prepare(query).all()

    var result = {}
    var players_key = "Players"
    result[players_key] = []

    for (i = 0; i < players.length; i++) {
        result[players_key].push(players[i])
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ------- Get players by name -------
   ----------------------------------- */

app.post("/get_players", (req, res, next) => {
    var name = req.body.name

    var query = "SELECT * from players WHERE name LIKE ('%' || ? || '%') LIMIT 30;"
    var players = db.prepare(query).all(name)

    var result = {}
    var players_key = "Players"
    result[players_key] = []

    for (i = 0; i < players.length; i++) {
        result[players_key].push(players[i])
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ------- Add player to list --------
   ----------------------------------- */

function user_can_add_player(user_id, group_id) {
    var query = "SELECT has_confirmed_players from user_group WHERE user_id=? AND group_id=?"
    var user = db.prepare(query).get(user_id, group_id)

    return user.has_confirmed_players == 0
}

function player_is_available(player_name, group_id) {
    var query = "SELECT * from user_players WHERE group_id=? AND player_name=?"
    var player = db.prepare(query).get(group_id, player_name)

    return player == null
}

app.post("/add_player", (req, res, next) => {
    var player_name = req.body.name
    var group_id = req.body.group_id
    var user_id = req.body.user_id

    if (!user_can_add_player(user_id, group_id)) {
        const send = {
            result: "You already confirmed your players"
        }

        res.status(201).send(send)
        return
    } else if (!player_is_available(player_name, group_id)){
        const send = {
            result: "This player has already been selected"
        }

        res.status(202).send(send)
        return
    }

    var query = "INSERT INTO user_players(user_id, player_name, group_id, is_tradable) values(?,?,?, 1)"
    var result = db.prepare(query).run(user_id, player_name, group_id)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   -------- Get participants ---------
   ----------------------------------- */

app.post("/participants", (req, res, next) => {
    var group_id = req.body.group_id

    var query = "SELECT * from user_group where group_id=?"
    var participants = db.prepare(query).all(group_id)

    var result = {}
    var participants_key = "Participants"
    result[participants_key] = []

    for (i = 0; i < participants.length; i++) {
        console.log(participants[i])
        var query = "SELECT username from users where id=?"
        var user = db.prepare(query).get(participants[i].user_id)

        result[participants_key].push(user.username)
    }

    const send = {
        groups: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ---- Get participant's players ----
   ----------------------------------- */

app.post("/participant_players", (req, res, next) => {
    var group_id = req.body.group_id
    var participant_name = req.body.participant_name

    var user_id = db.prepare("SELECT id FROM users WHERE username=?").get(participant_name).id
    console.log(user_id)

    var query = "SELECT * from user_players where user_id =? AND group_id=?"
    var players = db.prepare(query).all(user_id, group_id)

    var result = {}
    var players_key = "Players"
    result[players_key] = []

    for (i = 0; i < players.length; i++) {
        console.log(players[i])
        result[players_key].push(players[i].player_name)
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   -- Confirm participant's players --
   ----------------------------------- */

app.post("/confirm_players", (req, res, next) => {
    var group_id = req.body.group_id
    var participant_name = req.body.participant_name

    var user_id = db.prepare("SELECT id FROM users WHERE username=?").get(participant_name).id

    var query = "UPDATE user_group SET has_confirmed_players=1 WHERE user_id=? AND group_id=?"
    var result = db.prepare(query).run(user_id, group_id)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   --- Delete participant's player ---
   ----------------------------------- */

function user_can_delete(user_id, group_id) {
    var query = "SELECT has_confirmed_players from user_group WHERE user_id=? AND group_id=?"
    var user = db.prepare(query).get(user_id, group_id)

    return user.has_confirmed_players == 0
}

app.post("/delete_player", (req, res, next) => {
    var group_id = req.body.group_id
    var player_name = req.body.player_name
    var user_id = req.body.user_id

    if (!user_can_delete(user_id, group_id)) {
        const send = {
            result: "You have already confirmed your players"
        }

        res.status(201).send(JSON.stringify(send))
        return
    }

    var query = "DELETE FROM user_players WHERE user_id=? AND group_id=? AND player_name=?"
    var result = db.prepare(query).run(user_id, group_id, player_name)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   --- Check if player is tradable ---
   ----------------------------------- */

app.post("/tradable_player", (req, res, next) => {
    var group_id = req.body.group_id
    var participant_name = req.body.participant_name
    var player_name = req.body.player_name

    var user_id = db.prepare("SELECT id FROM users WHERE username=?").get(participant_name).id

    var query = "SELECT is_tradable FROM user_players WHERE user_id=? AND group_id=? AND player_name=?"
    var result = db.prepare(query).get(user_id, group_id, player_name)

    if (result.is_tradable == 1) {
        const send = {
            result: "tradable"
        }

        res.status(200).send(JSON.stringify(send))
    } else {
        res.status(400).send()
    }
});

/* -----------------------------------
   ---- Get user tradable players ----
   ----------------------------------- */

app.post("/all_tradable_players", (req, res, next) => {
    var group_id = req.body.group_id
    var participant_name = req.body.participant_name

    var user_id = db.prepare("SELECT id FROM users WHERE username=?").get(participant_name).id

    var query = "SELECT player_name FROM user_players WHERE user_id=? AND group_id=? AND is_tradable=1"
    var players = db.prepare(query).all(user_id, group_id)

    var result = {}
    var players_key = "Players"
    result[players_key] = []

    for (i = 0; i < players.length; i++) {
        console.log(players[i])
        result[players_key].push(players[i].player_name)
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   --------- Exchange players --------
   ----------------------------------- */

app.post("/exchange_players", (req, res, next) => {
    var source_user = req.body.source_user
    var target_user = req.body.target_user
    var group_id = req.body.group_id
    var group_name = req.body.group_name
    var source_player_name = req.body.source_player_name
    var target_player_name = req.body.target_player_name

    var src_user_id = db.prepare("SELECT id FROM users WHERE username=?").get(source_user).id
    var trg_user_id = db.prepare("SELECT id FROM users WHERE username=?").get(target_user).id

    //Set players not tradable
    var query = "UPDATE user_players SET is_tradable=0 WHERE user_id=? AND group_id=? AND player_name=?"
    var result = db.prepare(query).run(src_user_id, group_id, source_player_name)

    var query = "UPDATE user_players SET is_tradable=0 WHERE user_id=? AND group_id=? AND player_name=?"
    var result = db.prepare(query).run(trg_user_id, group_id, target_player_name)

    //Insert invitation in database
    var query = "INSERT INTO invitations_exchange(src_user_id, trg_user_id, group_id, group_name, " +
                "src_player_name, trg_player_name) values (?, ?, ?, ?, ?, ?)"
    var result = db.prepare(query).run(src_user_id, trg_user_id, group_id,
                                       group_name, source_player_name, target_player_name)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ------- Exchange invitations ------
   ----------------------------------- */

app.post("/exchange_invitations", (req, res, next) => {
    var user_id = req.body.user_id

    var query = 'SELECT * from invitations_exchange where trg_user_id=?;'
    var invitations = db.prepare(query).all(user_id)

    var result = {}
    var invitation_key = "Invitations"
    result[invitation_key] = []

    for (i = 0; i < invitations.length; i++) {
        result[invitation_key].push(invitations[i])
    }

    const send = {
        result: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ----------- Get username ----------
   ----------------------------------- */

app.post("/username", (req, res, next) => {
    var user_id = req.body.user_id

    var query = 'SELECT username from users where id=?;'
    var username = db.prepare(query).get(user_id).username

    const send = {
        result: username
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ---------- Accept exchange --------
   ----------------------------------- */

app.post("/accept_exchange", (req, res, next) => {
    var invitation_id = req.body.invitation_id
    var src_user_id = req.body.src_user_id
    var trg_user_id = req.body.trg_user_id
    var src_player_name = req.body.src_player_name
    var trg_player_name = req.body.trg_player_name
    var group_id = req.body.group_id

    var query = 'DELETE FROM invitations_exchange WHERE id=?;'
    var result = db.prepare(query).run(invitation_id)

    var query = 'UPDATE user_players SET player_name=?, is_tradable=1 WHERE user_id=? AND group_id=? AND player_name=?'
    var result = db.prepare(query).run(src_player_name, trg_user_id, group_id, trg_player_name)

    var query = 'UPDATE user_players SET player_name=?, is_tradable=1 WHERE user_id=? AND group_id=? AND player_name=?'
    var result = db.prepare(query).run(trg_player_name, src_user_id, group_id, src_player_name)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   --------- Decline exchange --------
   ----------------------------------- */

app.post("/decline_exchange", (req, res, next) => {
    var invitation_id = req.body.invitation_id
    var src_user_id = req.body.src_user_id
    var trg_user_id = req.body.trg_user_id
    var src_player_name = req.body.src_player_name
    var trg_player_name = req.body.trg_player_name
    var group_id = req.body.group_id

    var query = 'DELETE FROM invitations_exchange WHERE id=?;'
    var result = db.prepare(query).run(invitation_id)

    var query = 'UPDATE user_players SET is_tradable=1 WHERE user_id=? AND group_id=? AND player_name=?'
    var result = db.prepare(query).run(trg_user_id, group_id, trg_player_name)

    var query = 'UPDATE user_players SET is_tradable=1 WHERE user_id=? AND group_id=? AND player_name=?'
    var result = db.prepare(query).run(src_user_id, group_id, src_player_name)

    const send = {
        result: "success"
    }

    res.status(200).send(JSON.stringify(send))
});

/* -----------------------------------
   ---- Get participant's points ----
   ----------------------------------- */

app.post("/get_points", (req, res, next) => {
    var group_id = req.body.group_id
    var participant_name = req.body.participant_name

    var user_id = db.prepare("SELECT id FROM users WHERE username=?").get(participant_name).id

    var query = "SELECT Sum(goals) as buts from players p where p.name in (Select player_name from user_players u where u.group_id=? and u.user_id=?)"
    var goals = db.prepare(query).all(group_id, user_id)

    var query = "SELECT SUM(assists) as passes from players p where p.name in (Select player_name from user_players u where u.group_id=? and u.user_id=?)"
    var pass = db.prepare(query).all(group_id, user_id)

    if(goals[0].buts == null) {
        console.log("goals null")
        goals = []

        temp = {
            buts: 0
        }

        goals.push(temp)
    }

    if(pass[0].passes == null) {
        console.log("passes null")
        pass = []

        temp = {
            passes: 0
        }

        pass.push(temp)
    }

    var query = "SELECT username FROM users where id=?"
    var name = db.prepare(query).all(user_id)

    const result = {
        goals,
        pass,
        name
    }

    const send = {
       groups: JSON.stringify(result)
    }
    console.log(send)

    res.status(200).send(JSON.stringify(send))
});

app.post("/get_points_group", (req, res, next) => {
    console.log("Groups")
    var group_id = req.body.group_id

    var query = "SELECT goal_value from groups where id=?"
    var goal_value = db.prepare(query).all(group_id)

    var query = "SELECT pass_value from groups where id=? "
    var pass_value = db.prepare(query).all(group_id)

    const result = {
        goal_value,
        pass_value
    }
    const send = {
       groups: JSON.stringify(result)
    }

    res.status(200).send(JSON.stringify(send))
});

app.use(function(req, res){
    res.status(404)
});
