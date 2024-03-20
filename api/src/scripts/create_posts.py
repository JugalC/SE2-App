import requests


API_URL = "http://[::1]:8080/"
ALL_USERS_ENDPOINT = "users"
GET_RECENT_SONG_ENDPOINT = "most_recent_song"

def get_list_of_valid_user_ids():
    response = requests.get(f"{API_URL}{ALL_USERS_ENDPOINT}").json()

    valid_user_ids = []
    for user_account in response:
        if (user_account['spotifyAccessToken'] != None) and (user_account['spotifyRefreshToken'] != None):
            valid_user_ids.append(user_account['id'])

    return valid_user_ids

def get_most_recent_song(user_id):
    response = requests.get(f"{API_URL}{GET_RECENT_SONG_ENDPOINT}/{user_id}").json()
    if 'album_name' in response.keys():
        return f"Success {user_id}"
    else:
        return f"Failed {user_id}"


def run():
    valid_user_ids = get_list_of_valid_user_ids()
    for user_id in valid_user_ids:
        return_msg = get_most_recent_song(user_id)
        print(return_msg)

if __name__ == "__main__":
    run()