```
Database: World {
    Collection: Users {
        UUID {
            name: user_name
            approved: true
            plots {
                x:z {
                    meta?
                }
            }
        }
    }
    Collection: Plots {
        x:z {
            alias: name
            owner: UUID
            comments [
                "some f*cking opinion"
            ]
        }
    }
}
```
