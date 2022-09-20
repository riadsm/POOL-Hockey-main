create table users (
  id integer primary key,
  username varchar(20),
  email varchar(40),
  password varchar(15)
);

create table user_group (
  id integer primary key,
  user_id integer,
  group_id integer,
  admin_rights boolean,
  has_confirmed_players boolean,
  FOREIGN KEY(user_id) REFERENCES users(id),
  FOREIGN KEY(group_id) REFERENCES groups (id)
);

create table groups (
  id integer primary key,
  name varchar(20),
  goal_value integer,
  pass_value integer,
  prize_value varchar(100)
);

create table invitations_group (
  id integer primary key,
  user_id integer,
  group_id integer,
  group_name varchar(20),
  FOREIGN KEY(user_id) REFERENCES users(id)
);

create table invitations_exchange (
  id integer primary key,
  src_user_id integer,
  trg_user_id integer,
  group_id integer,
  group_name varchar(20),
  src_player_name varchar(50),
  trg_player_name varchar(50),
  FOREIGN KEY(src_user_id) REFERENCES users(id)
);

create table teams (
  id integer primary key,
  name varchar(50),
  team_id integer
);

create table players(
  id integer primary key,
  name varchar(50),
  team_name varchar(50),
  team_id integer,
  jersey_number integer,
  position varchar(50),
  time_on_ice varchar (15),
  assists integer,
  goals integer,
  shots integer,
  games integer
);


create table user_players(
  id integer primary key,
  user_id integer,
  player_name varchar(50),
  group_id integer,
  is_tradable boolean
);

/* USERS */
insert into users(username, email, password) values ('admin', 'admin@email.com', 'admin');
insert into users(username, email, password) values ('Paul', 'paul@email.com', 'password_paul');
insert into users(username, email, password) values ('Fred', 'fred@email.com', 'password_fred');
insert into users(username, email, password) values ('Riad', 'riad@email.com', 'password_riad');
insert into users(username, email, password) values ('test', 'test@email.com', 'test');

/* GROUPS */
insert into groups(name, goal_value, pass_value, prize_value) values ('first_group', 1, 1, 'Un souper au restaurant');
insert into groups(name, goal_value, pass_value, prize_value) values ('second_group', 1, 1, 'Une partie de golf');
insert into groups(name, goal_value, pass_value, prize_value) values ('third_group', 1, 1, 'Une bonne bouteille de vin');

/* USERS IN GROUPS */
insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values ('1', '1', '1', '0');
insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values ('2', '1', '0', '0');
insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values ('4', '1', '0', '0');

insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values ('1', '2', '1', '0');
insert into user_group(user_id, group_id, admin_rights, has_confirmed_players) values ('1', '3', '1', '0');

/* INVITATIONS */
insert into invitations_group(user_id, group_id, group_name) values ('5', '1', 'first_group');
insert into invitations_group(user_id, group_id, group_name) values ('5', '2', 'second_group');
insert into invitations_group(user_id, group_id, group_name) values ('5', '2', 'third_group');