```
Database: World {
    Collection: users {
        UUID {
            name: user_name
            approved: true
            plots [
                x:z
            ]
        }
    }
    Collection: plots {
        {
            id: x:z
            alias: name
            owner: UUID
            comments [
                "some f*cking opinion"
            ]
        }
    }
}
```
