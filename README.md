# Budgethanteraren

## Description

This program is a command-line budget tool that tracks the user's transactions and balance.
The user can:

- add a transaction (income or expense)
- show all stored transactions
- show the balance and a summary per category
- filter transactions by type or between two dates
- save all transactions to a file

When the program starts, the stored transactions are loaded from the file, so the user
doesn't have to add them again each time.



### Bug 1: Wrong field index in `parseLine`

I found this bug through tests in FileHandlerTest. There were 6 tests that didn't pass, including `parseLine_validLine_returnsTransaction`  which threw an exception and `load_fileWithValidLines_returnsTransactionsInOrder` which returned an empty list instead of a list with 2 transactions. The error message said "FileFormatException "Invalid transaction amount: Salary"" and the load test returned an empty list because the `load` method skips lines that fail to parse.

Since the message said "Salary" instead of the amount I realised that something was causing the problem in the `parseLine` method and more specifically the amount part of the method. This was also supported by the logs since the DEBUG line said "Parsing line: 2026-03-12,INCOME,Salary,100" which shows that the line itself was correct. But that was followed by a WARN line which said "WARN Skipping line 1: Invalid transaction amount: Salary".

That led me to set a breakpoint on the `amount = Double.parseDouble(parts[2]);` line and run the tests again but using debug mode. The debugger showed that `parts[2]` was equal to "Salary" whilst the amount (100) was actually in `parts[3]`.

The cause behind it is that a line has a total of 4 fields (date, type, category and amount), where the amount is at index 3. But since the code read index 2 instead of index 3, it tried to use the category in place of the amount. The fix was to change the `amount = Double.parseDouble(parts[2]);` line to `amount = Double.parseDouble(parts[3]);`. After the change the tests passed.



### Bug 2: Trailing comma ignored by `split` in `parseLine`

I also found this bug through the tests in FileHandlerTest, but it only showed after i fixed the bug mentioned above. The test that failed was `parseLine_trailingCommaAfterAmount_throwsFileFormatException` which was expected to throw an exception, but nothing was thrown.

I decided to do the same as i did for the previouse bug and added a breakpoint. This time I added it to the `if (parts.length != 4)` line. When I then ran the test again using debug mode, it showed the line say "2026-03-12,INCOME,Salary,100," but also that `parts.length` was 4 (date, type, category, amount) instead of 5 (date, type, category, amount , "empty").

This caused the if-statement (`if (parts.length != 4)`) in the `parseLine` method to pass as false. This happened due to `split`, since when split is called with just the regex argument it removes the empty strings. To fix it, i used `split` but added a limit of -1 as an argument (`line.split(",", -1)`) which tells it to not drop the trailing empty strings and `parts[]` ends up with a length of 5. After the change this test also passed.





### Generics (Repository<T>)

Because `Repository<T>` uses the placeholder `T` instead of a specific type, the same class can store transactions today and any other type later, without writing a new repository class for each one. Without generics I would have needed either one repository per type or a repository of `Object`.

If the repository had been of `Object` it would then introduce the need for typecasting every time something is read from it. That would also increase the likelihood of the program crashing since a wrong typecast only fails at runtime. But with `Repository<T>` the compiler checks the type, making typecasting unnecessary and in the case the wrong type is used, it is caught during compilation.

Another example of why the use of generics made the code more reusable is the `findWhere` method. This method is used to filter the list in the repository for entries that match a specified condition. The `Predicate` used as an argument, allows us to pass in the condition in the method call instead of writing it in the method body and locking the method’s use to that specific condition. This makes it reusable so that if later, someone wanted to add another filter they wouldn't need to rewrite the entire method. The alternative would be to have one method with its own loop per filter instead of passing in the condition. That would make the code less reusable and also harder to read and understand.



### Stream API

One example of where I used streams is in the `Summary` class's `balance` and `sumPerCategory` methods. In the `balance` method the stream is used to calculate the total sum of all the transactions and returns the final sum as a double. In the `sumPerCategory` method the stream is used to first split the transactions into groups by their `category` and then calculate the sum of the transactions in each of them. This is done by first using `Collectors.groupingBy` which takes a function (`Transaction::category`) and uses the result as the key to group objects in a map. `Collectors.summingDouble` then adds up the amount of every transaction in each group. The result is a map where the category (`String`) is the key and the sum of its amounts (`Double`) is the value. Both streams also use a method call to the `signedAmount` method which is used to determine wether the amount should be either positive if the `TransactionType` is `INCOME` and negative if the type is `EXPENSE`.

Also as mentioned the Generics section, I used a stream in the `findWhere` method in `Repository<T>`. Which uses filter with the `Predicate` passed in the method call to find transactions that match.

If I had instead decided to make for example the `sumPerCategory` method without using a stream, I would have had to loop through the `Transaction` list using a loop. Where during each iteration I would need to determine whether that transaction’s `category` already exists in the map, before either adding or subtracting the amount to the categories sum. This would make the code longer and less straightforward. A stream is made by describing what you want, in this case group by category and sum the amounts, while a loop describes how to do it step by step. Though for someone that doesnt know how streams work, the loop would be far easier to understand since it would describe the whole process.
