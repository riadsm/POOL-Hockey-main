import sqlite3
import json
import os
import requests

base_url = 'https://statsapi.web.nhl.com'
params_url = '/stats?stats=statsSingleSeason&season=20202021'

abspath = os.path.abspath(__file__)
dname = os.path.dirname(abspath)
os.chdir(dname)


def clear_database(db):
    cursor = db.cursor()
    cursor.execute('DELETE FROM players;')
    cursor.execute('DELETE FROM teams')

def insert_team(db, name, team_id):
    cursor = db.cursor()
    teams = cursor.execute('INSERT INTO teams(name, team_id) values(?, ?)', [name, team_id])


def insert_player(db, name, team_name, team_id, jersey_number, position, time_on_ice, assists, goals,shots, games):
    cursor = db.cursor()
    teams = cursor.execute('INSERT INTO players(name, team_name, team_id, jersey_number, position, time_on_ice, assists, goals,shots, games) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', 
            [name, team_name, team_id, jersey_number, position, time_on_ice, assists, goals,shots, games])


def fetchPlayerData(player_url):
    request = requests.get(player_url)
    return request.json()["people"][0]


def fetchPlayerStats(player_url):
    request = requests.get(player_url)

    try:
        result = request.json()["stats"][0]["splits"][0]["stat"]
        return result
    except:
        return ""


def insertPlayer(db, player_data, player_stats, jersey_number):
    name = player_data["fullName"] if "fullName" in player_data else ""
    team_name = player_data["currentTeam"]["name"] if "name" in player_data["currentTeam"] else ""
    team_id = player_data["currentTeam"]["id"] if "id" in player_data["currentTeam"] else ""
    position = player_data["primaryPosition"]["name"] if "name" in player_data["primaryPosition"] else ""
    time_on_ice = player_stats["timeOnIce"] if "timeOnIce" in player_stats else ""
    assists = player_stats["assists"] if "assists" in player_stats else ""
    goals = player_stats["goals"] if "goals" in player_stats else ""
    shots = player_stats["shots"] if "shots" in player_stats else ""
    games = player_stats["games"] if "games" in player_stats else ""

    insert_player(db,
        name,
        team_name,
        team_id,
        jersey_number,
        position,
        time_on_ice,
        assists,
        goals,
        shots,
        games)


def fetchTeamPlayers(db, team_url):
    request = requests.get(team_url)
    roster = request.json()["roster"]

    for i in range(0, len(roster)): #len(roster)
        player_data = fetchPlayerData(base_url + roster[i]["person"]["link"])
        player_stats = fetchPlayerStats(base_url + roster[i]["person"]["link"] + params_url)

        if player_stats == "":
            continue
        
        insertPlayer(db, player_data, player_stats, roster[i]["jerseyNumber"])



def fetchData(db):
    for i in range(0, len(teams)): #len(teams)
        print("Team number: " + str(i + 1) + " out of " + str(len(teams)) + " " + teams[i]["name"])
        insert_team(db, teams[i]["name"], teams[i]["id"])

        team_url = base_url + teams[i]["link"] + '/roster'
        fetchTeamPlayers(db, team_url)
    print ("Done!")

with sqlite3.connect('./database.db') as db:
    clear_database(db)
    request = requests.get('https://statsapi.web.nhl.com/api/v1/teams')
    teams = request.json()["teams"]
    fetchData(db)
    db.commit()
