# R1: Confirm Yahoo Fantasy API Response Structure

**Feature:** [league-ingestion](https://github.com/Ethan-Personal/FB-Yahoo/issues/23)  
**Status:** 🔍 In Progress

---

## Goal

Validate the exact JSON response structure from Yahoo Fantasy API for user leagues and league teams endpoints to ensure DTO design is accurate.

---

## Research Questions & Answers

### Q1: What is the exact JSON structure returned by `/fantasy/v2/users;use_login=1/games;game_keys=nba/leagues?format=json`?

**Answer:**

### Example: User Leagues Response

```json
{
  "fantasy_content": {
    "xml:lang": "en-US",
    "yahoo:uri": "/fantasy/v2/users;use_login=1/games;game_keys=nba/leagues",
    "users": {
      "0": {
        "user": [
          {
            "guid": "D2ZH42I3APBCXIMCZWNAYKHYNU"
          },
          {
            "games": {
              "0": {
                "game": [
                  {
                    "game_key": "466",
                    "game_id": "466",
                    "name": "Basketball",
                    "code": "nba",
                    "type": "full",
                    "url": "https://basketball.fantasysports.yahoo.com/nba",
                    "season": "2025",
                    "is_registration_over": 0,
                    "is_game_over": 0,
                    "is_offseason": 0
                  },
                  {
                    "leagues": {
                      "0": {
                        "league": [
                          {
                            "league_key": "466.l.42086",
                            "league_id": "42086",
                            "name": "Teen Center Adult Leauge",
                            "url": "https://basketball.fantasysports.yahoo.com/nba/42086",
                            "logo_url": "https://s.yimg.com/ep/cx/blendr/v2/image-basketball-3-png_1721241401648.png",
                            "draft_status": "postdraft",
                            "num_teams": 10,
                            "edit_key": "2026-02-03",
                            "weekly_deadline": "intraday",
                            "roster_type": "date",
                            "league_update_timestamp": "1770108633",
                            "scoring_type": "headone",
                            "league_type": "private",
                            "renew": "454_98442",
                            "renewed": "",
                            "felo_tier": "silver",
                            "is_highscore": false,
                            "matchup_week": 16,
                            "iris_group_chat_id": "",
                            "short_invitation_url": "https://basketball.fantasysports.yahoo.com/nba/42086/invitation?key=c93d3c7c7a014890&ikey=351b3c17068fbc79",
                            "allow_add_to_dl_extra_pos": 1,
                            "is_pro_league": "0",
                            "is_cash_league": "0",
                            "current_week": 16,
                            "start_week": "1",
                            "start_date": "2025-10-21",
                            "end_week": "22",
                            "end_date": "2026-03-29",
                            "current_date": "2026-02-03",
                            "is_plus_league": "0",
                            "game_code": "nba",
                            "season": "2025"
                          }
                        ]
                      },
                      "1": {
                        "league": [
                          {
                            "league_key": "466.l.213656",
                            "league_id": "213656",
                            "name": "Yahoo High Score 213656",
                            "url": "https://basketball.fantasysports.yahoo.com/nba/213656",
                            "logo_url": "https://s.yimg.com/cv/api/default/20180206/default-league-logo@2x.png",
                            "draft_status": "postdraft",
                            "num_teams": 10,
                            "edit_key": "2026-02-03",
                            "weekly_deadline": "intraday",
                            "roster_type": "week",
                            "league_update_timestamp": "1770108852",
                            "scoring_type": "headpoint",
                            "league_type": "public",
                            "renew": "",
                            "renewed": "",
                            "felo_tier": "silver",
                            "is_highscore": true,
                            "matchup_week": 16,
                            "iris_group_chat_id": "",
                            "short_invitation_url": "https://basketball.fantasysports.yahoo.com/nba/213656/invitation",
                            "allow_add_to_dl_extra_pos": 0,
                            "is_pro_league": "0",
                            "is_cash_league": "0",
                            "current_week": 16,
                            "start_week": "1",
                            "start_date": "2025-10-21",
                            "end_week": "23",
                            "end_date": "2026-04-05",
                            "current_date": "2026-02-03",
                            "is_plus_league": "0",
                            "game_code": "nba",
                            "season": "2025"
                          }
                        ]
                      },
                      "2": {
                        "league": [
                          {
                            "league_key": "466.l.331285",
                            "league_id": "331285",
                            "name": "Ethan's Excellent League",
                            "url": "https://basketball.fantasysports.yahoo.com/nba/331285",
                            "logo_url": "https://s.yimg.com/ep/cx/blendr/v2/image-basketball-3-png_1721241401648.png",
                            "password": "",
                            "draft_status": "predraft",
                            "num_teams": 1,
                            "edit_key": "2026-02-03",
                            "weekly_deadline": "intraday",
                            "roster_type": "date",
                            "league_update_timestamp": null,
                            "scoring_type": "head",
                            "league_type": "private",
                            "renew": "",
                            "renewed": "",
                            "felo_tier": "silver",
                            "is_highscore": false,
                            "matchup_week": 16,
                            "iris_group_chat_id": "",
                            "short_invitation_url": "https://basketball.fantasysports.yahoo.com/nba/331285/invitation?key=431c7fe0f68edba3&ikey=bc998a8d09f6f421",
                            "allow_add_to_dl_extra_pos": 1,
                            "is_pro_league": "0",
                            "is_cash_league": "0",
                            "current_week": 16,
                            "start_week": "4",
                            "start_date": "2025-11-10",
                            "end_week": "23",
                            "end_date": "2026-04-05",
                            "current_date": "2026-02-03",
                            "is_plus_league": "0",
                            "game_code": "nba",
                            "season": "2025"
                          }
                        ]
                      },
                      "count": 3
                    }
                  }
                ]
              },
              "count": 1
            }
          }
        ]
      },
      "count": 1
    },
    "time": "66.784143447876ms",
    "copyright": "Certain Data by Sportradar, Stats Perform and Rotowire",
    "refresh_rate": "60"
  }
}
```


**Notes:**

---

### Q2: What is the exact JSON structure returned by `/fantasy/v2/league/{league_key}/teams?format=json`?

**Answer:**

### Example: League Teams Response

```json
{
  "fantasy_content": {
    "xml:lang": "en-US",
    "yahoo:uri": "/fantasy/v2/league/466.l.42086/teams",
    "league": [
      {
        "league_key": "466.l.42086",
        "league_id": "42086",
        "name": "Teen Center Adult Leauge",
        "url": "https://basketball.fantasysports.yahoo.com/nba/42086",
        "logo_url": "https://s.yimg.com/ep/cx/blendr/v2/image-basketball-3-png_1721241401648.png",
        "draft_status": "postdraft",
        "num_teams": 10,
        "edit_key": "2026-02-03",
        "weekly_deadline": "intraday",
        "roster_type": "date",
        "league_update_timestamp": "1770108633",
        "scoring_type": "headone",
        "league_type": "private",
        "renew": "454_98442",
        "renewed": "",
        "felo_tier": "silver",
        "is_highscore": false,
        "matchup_week": 16,
        "iris_group_chat_id": "",
        "short_invitation_url": "https://basketball.fantasysports.yahoo.com/nba/42086/invitation?key=c93d3c7c7a014890&ikey=351b3c17068fbc79",
        "allow_add_to_dl_extra_pos": 1,
        "is_pro_league": "0",
        "is_cash_league": "0",
        "current_week": 16,
        "start_week": "1",
        "start_date": "2025-10-21",
        "end_week": "22",
        "end_date": "2026-03-29",
        "current_date": "2026-02-03",
        "is_plus_league": "0",
        "game_code": "nba",
        "season": "2025"
      },
      {
        "teams": {
          "0": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.1"
                },
                {
                  "team_id": "1"
                },
                {
                  "name": "Young & BiCURRYous"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/1"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://yahoofantasysports-res.cloudinary.com/image/upload/t_s192sq/fantasy-logos/4e0b6bdfe38140473e722af483a12b57d76cec273e158227cd342c23656e26ee.jpg"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 7
                },
                [],
                {
                  "waiver_priority": 1
                },
                [],
                {
                  "number_of_moves": 18
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                {
                  "clinched_playoffs": 1
                },
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 10
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "1",
                        "nickname": "Tevin",
                        "guid": "O2YELKLRYGVV5ZW2DPR4UL4CD4",
                        "is_commissioner": "1",
                        "email": "teving21@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "834",
                        "felo_tier": "platinum"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "1": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.2"
                },
                {
                  "team_id": "2"
                },
                {
                  "name": "Chet the Freak Out"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/2"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://s.yimg.com/ep/cx/blendr/v2/image-wizard-1-png_1729555804139.png"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 1
                },
                [],
                {
                  "waiver_priority": 10
                },
                [],
                {
                  "number_of_moves": 31
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "1"
                  }
                },
                {
                  "clinched_playoffs": 1
                },
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 7
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "2",
                        "nickname": "Caitlin",
                        "guid": "4Y3KQWF6RAVCKDJCE47H7S5YV4",
                        "email": "caitlinfu@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/e4d2c9cc-cc2b-43c6-8eed-4322df9471ebv1_64sq.jpg",
                        "felo_score": "888",
                        "felo_tier": "platinum"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "2": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.3"
                },
                {
                  "team_id": "3"
                },
                {
                  "name": "Ethan's Expert Team"
                },
                {
                  "is_owned_by_current_login": 1
                },
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/3"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://s.yimg.com/ep/cx/blendr/v2/image-hot-dog-png_1721176248469.png"
                      }
                    }
                  ]
                },
                [],
                [],
                {
                  "waiver_priority": 5
                },
                [],
                {
                  "number_of_moves": 0
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 5
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "3",
                        "nickname": "Ethan",
                        "guid": "D2ZH42I3APBCXIMCZWNAYKHYNU",
                        "is_current_login": "1",
                        "email": "ethankook1@yahoo.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "615",
                        "felo_tier": "silver"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "3": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.4"
                },
                {
                  "team_id": "4"
                },
                {
                  "name": "Ja’s Shootas"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/4"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://yahoofantasysports-res.cloudinary.com/image/upload/t_s192sq/fantasy-logos/b95d3beec2d9d120a25e0ccc65202e41113e65d895d9c553e87433d9627f3347.jpg"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 8
                },
                [],
                {
                  "waiver_priority": 2
                },
                [],
                {
                  "number_of_moves": 5
                },
                {
                  "number_of_trades": "1"
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 9
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "4",
                        "nickname": "Jocelyn",
                        "guid": "43N77K2IALU5YZSYZQBQ24WPVE",
                        "email": "jocenakamine@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "596",
                        "felo_tier": "bronze"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "4": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.5"
                },
                {
                  "team_id": "5"
                },
                {
                  "name": "Bryan's Boss Team"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/5"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://s.yimg.com/cv/apiv2/default/nba/nba_6_b.png"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 10
                },
                [],
                {
                  "waiver_priority": 7
                },
                [],
                {
                  "number_of_moves": 17
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                {
                  "clinched_playoffs": 1
                },
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 4
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "5",
                        "nickname": "Bryan",
                        "guid": "OS3JAZCUD56H42Y6G2K2B6MBVE",
                        "email": "bryanwongie1016@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "676",
                        "felo_tier": "silver"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "5": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.6"
                },
                {
                  "team_id": "6"
                },
                {
                  "name": "Oh My Goodness Valentine"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/6"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://yahoofantasysports-res.cloudinary.com/image/upload/t_s192sq/fantasy-logos/d2914d3febd57f7747748366fb291a83ee78105cae1ac1f2e0247a6e073badad.jpg"
                      }
                    }
                  ]
                },
                [],
                [],
                {
                  "waiver_priority": 6
                },
                [],
                {
                  "number_of_moves": 0
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 2
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "6",
                        "nickname": "Kailey",
                        "guid": "FBN2IY3TKSUN63VC44355WK7NM",
                        "email": "kalegarden@yahoo.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "556",
                        "felo_tier": "bronze"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "6": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.7"
                },
                {
                  "team_id": "7"
                },
                {
                  "name": "Free Th(Rosa) Parks"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/7"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://yahoofantasysports-res.cloudinary.com/image/upload/t_s192sq/fantasy-logos/f10c388f0bea76e6f3b13d636e726577aca0fc3a060b42690affbc102a4b513e.jpg"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 6
                },
                [],
                {
                  "waiver_priority": 3
                },
                [],
                {
                  "number_of_moves": 8
                },
                {
                  "number_of_trades": "1"
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 8
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "7",
                        "nickname": "william",
                        "guid": "SOO4DY3JQ3H42HD7CUD7REHA3A",
                        "email": "willdaylo@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "618",
                        "felo_tier": "silver"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "7": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.8"
                },
                {
                  "team_id": "8"
                },
                {
                  "name": "Jokic on this dick"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/8"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://s.yimg.com/ep/cx/blendr/v2/image-ogre-1-png_1729555655805.png"
                      }
                    }
                  ]
                },
                [],
                [],
                {
                  "waiver_priority": 8
                },
                [],
                {
                  "number_of_moves": 9
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                {
                  "clinched_playoffs": 1
                },
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 1
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "8",
                        "nickname": "Casey",
                        "guid": "GAVL5ZIUHIIBEJFERY75RELYSI",
                        "email": "puacasey@yahoo.com",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "717",
                        "felo_tier": "gold"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "8": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.9"
                },
                {
                  "team_id": "9"
                },
                {
                  "name": "G-League Dreamers"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/9"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://s.yimg.com/cv/apiv2/default/nba/nba_10_n.png"
                      }
                    }
                  ]
                },
                {
                  "previous_season_team_rank": 9
                },
                [],
                {
                  "waiver_priority": 4
                },
                [],
                {
                  "number_of_moves": 0
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 6
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "9",
                        "nickname": "Najel",
                        "guid": "WLSKMENFI6O6IO2F3YKXIXD5HE",
                        "email": "najelalarcon@gmail.com",
                        "image_url": "https://s.yimg.com/ag/images/4536/38215908094_3635fd_64sq.jpg",
                        "felo_score": "513",
                        "felo_tier": "bronze"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "9": {
            "team": [
              [
                {
                  "team_key": "466.l.42086.t.10"
                },
                {
                  "team_id": "10"
                },
                {
                  "name": "LeBron’s left nut"
                },
                [],
                {
                  "url": "https://basketball.fantasysports.yahoo.com/nba/42086/10"
                },
                {
                  "team_logos": [
                    {
                      "team_logo": {
                        "size": "large",
                        "url": "https://yahoofantasysports-res.cloudinary.com/image/upload/t_s192sq/fantasy-logos/760517250e70eff885732470ff3c635ee2b8bb64669ff02ba29f5f7900149a4c.jpg"
                      }
                    }
                  ]
                },
                [],
                [],
                {
                  "waiver_priority": 9
                },
                [],
                {
                  "number_of_moves": 12
                },
                {
                  "number_of_trades": 0
                },
                {
                  "roster_adds": {
                    "coverage_type": "week",
                    "coverage_value": 16,
                    "value": "0"
                  }
                },
                [],
                {
                  "league_scoring_type": "headone"
                },
                [],
                {
                  "draft_position": 3
                },
                {
                  "has_draft_grade": 0
                },
                [],
                [],
                [],
                [],
                [],
                {
                  "managers": [
                    {
                      "manager": {
                        "manager_id": "10",
                        "nickname": "Tyler",
                        "guid": "ZEMNYIKZUSRTGVV7ZP6NQ6RXOA",
                        "image_url": "https://s.yimg.com/ag/images/default_user_profile_pic_64sq.jpg",
                        "felo_score": "580",
                        "felo_tier": "bronze"
                      }
                    }
                  ]
                }
              ]
            ]
          },
          "count": 10
        }
      }
    ],
    "time": "145.19691467285ms",
    "copyright": "Certain Data by Sportradar, Stats Perform and Rotowire",
    "refresh_rate": "60"
  }
}
```
**Notes:**

---

### Q3: Are there any wrapper elements (e.g., `fantasy_content`) around the data?

**Answer:** Yes

**Notes:**

---

### Q4: What are the exact field names and nesting for league and team objects?

**Answer:** JSON response is provided above, see "league" and "team" objects.

**Notes:**

---

## Deliverables

docs/research/league-ingestion/R1-yahoo-api-response-structure.md with example JSON responses and mapping to proposed DTOs.

---

## References

None
