# Basic implementation of money transfer between accounts

The goal of this project is to design and implement a RESTful API (including data model and the backing implementation) for money
                               transfers between accounts. 
                               
### Technical Stack ###
To implement this solution, the following libraries / tools were used:
  * Editor/Tool - Intellij IDEA  
  * Java 8 - Programming language   
  * Maven 3.5.4 - Used for dependency management and as a build tool.  
  * [SparkJava](http://sparkjava.com/) - Implementing the rest api
  * jUnit - unit test framework.
  * RestAssured - for API testing.
  * Git - Version control system.                               

### Rest Api Route(s)
Please check the api definition here: [revolut-moneytransfer-api.yaml](api/revolut-moneytransfer-api.yaml)
#### Accounts
```
get(localhost:4567/allAccounts") -> Get all accounts
```
```
get(localhost:4567/allAccounts?flag=ACTIVE") -> Get all active accounts
```

```
get(localhost:4567/allAccounts?flag=INACTIVE") -> Get all inactive accounts
```

```
get(localhost:4567/account/:id") -> Get an account by id
```

```
post(localhost:4567/account/") -> Create an account

{
	"accountHolder":"Revolut",
	"balance": {
        "amount": 200,
        "currency": "EUR"
    }
}
```

```
put(localhost:4567/updateAccountInfo") -> Update an account information

	{
        "accountId":1,
    	"accountHolder":"Revolut Bank"
    }

```

```
put(localhost:4567/updateAccountInfo") -> Update an account status

	{
    	"accountId":1,
        "status":"ACTIVE"
    }

```

```
put(localhost:4567/updateAccountBalance/credit") -> Update an account balance, do credit

	{
    	"accountId":1,
        "balance": {
                "amount": 240,
                "currency": "PLN"
            }
    }

```
```
put(localhost:4567/updateAccountBalance/debit") -> Update an account balance, do debit

	{
    	"accountId":1,
        "balance": {
                "amount": 240,
                "currency": "PLN"
            }
    }

```

#### Transfers
```
get(localhost:4567/alltransfer") -> Get all transfers
```

```
get(localhost:4567/alltransfer?accountId=3") -> Get all transfers by an account id
```

```
post(localhost:4567/transfer") -> Create a new transfer
{
	"sourceAccountId":1,
	"destinationAccountId":2,
	"amount": {
        "amount": 13,
        "currency": "EUR"
    },
	"description":"Testing"
}
```

An example of a account payload
```$xslt
{
		"accountId": 2,
		"accountHolder": {
			"name": "dfasasa12s"
		},
		"balance": {
			"amount": 226,
			"currency": "EUR"
		},
		"status": "ACTIVE",
		"created": "2018-09-21T04:32:53.991+02:00"
	}
```


An example of a transfer payload
```$xslt
{
	"transferId": "44fe4c66-3b1f-461c-ae78-a2f69e90f006",
	"sourceAccountId": 1,
	"destinationAccountId": 2,
	"amount": {
		"amount": 13,
		"currency": "EUR"
	},
	"transferDate": "2018-09-21T04:33:45.571+02:00",
	"description": "Testing"
}
```

## Build whole project
```
mvn clean package 
```
or

```
mvn clean install 
```

## Run 
```$xslt
mvn exec:java
``` 
Before performing the above command make sure the port `4567` is not in used.

## Future Improvement and TODO(s)
* Use in-memory databases for better storage and testing
* Introducing different types of user account
* Allow credit and debit operation between different currencies
* Allow transactions between accounts with different currencies
* Introduce currency conversion options
* Introduce time-scheduled based transactions

