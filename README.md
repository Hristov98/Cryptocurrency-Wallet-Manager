Note: Replace the string constant API_KEY in the server's CoinAPI class with your personal CoinAPI key if you wish to run the project fully.

TODO list of optimisations and expansion ideas:
1. Add confirmation to buy/sell, so the user can know how much crypto he'll receive or how much it will sell for.
2. Create a logger class to save exception and errors to a file
3. Let the user be able to send requests with their own CoinAPI API key to go around the 100 requests limit
4. Save the list of cryptocurrencies to a file along with users to optimise number of requests sent
5. Mock CoinAPI and Client/Server for proper testing