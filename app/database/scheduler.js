const db = require('better-sqlite3')('./app/database/database.db')
const schedule = require('node-schedule')
var request = require('sync-request');

let base_url = 'https://statsapi.web.nhl.com'
let params_url = '/stats?stats=statsSingleSeason&season=20202021'

//When the minute is XX
const job = schedule.scheduleJob('01 * * * *', function(){
  console.log('Started scheduled job.')
  fetchData()
});

//Every 5 seconds for testing
//const job = schedule.scheduleJob('*/5 * * * * *', function(){
 /* console.log('Starting scheduled job.')
  fetchData()
  console.log('Job ended.')
});*/

function fetchData() {
    const { exec } = require("child_process");

    exec("python ./app/database/fetchData.py", (error) => {
        if (error) {
            console.log(`error: ${error.message}`);
            return;
        }
    });
}
