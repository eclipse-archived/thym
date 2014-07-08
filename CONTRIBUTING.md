## How to contribute

Contributions are essential for keeping Thym great.
There are a few guidelines that [Eclipse.org contributors](https://www.eclipse.org/contribute/) needs 
to follow so that we can have a chance of keeping on
top of things.

## Getting Started

* Make sure you have a completed and signed [Eclipse CLA](https://www.eclipse.org/legal/CLA.php)
* Make sure you have a [GitHub account](https://github.com/signup/free)
* [Fork](http://help.github.com/forking/) the repository on GitHub


## Making changes

* Clone your fork
````
    $ git clone git@github.com:<you>/thym.git
    $ cd thym
    $ git remote add upstream git@github.com:eclipse/thym.git
````  
At any time, you can pull changes from the upstream and merge them onto your master.  The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

    $ git checkout master               # switches to the 'master' branch
    $ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
    $ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

   
* Create a topic branch based on master, Please avoid working directly on the
````
   $ git checkout -b my_contribution
````
* Make changes for the bug or feature.
* Make sure you have added the necessary tests for your changes.
* Make sure that a full build (with unit tests) runs successfully. 
* Commit your changes and have your commit messages in the proper format
   For Example:
````
    [410937] Auto share multiple projects in single job
    
    When multiple projects are imported together, perform all the necessary
    auto shares in a single job rather than spawning a separate job for each
    project.
    
    Bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=410937
    Also-by: Some Otherperson <otherperson@someplace.net>
    Signed-off-by: Joe Somebody <somebody@someplace.net>
````
   Note that the "Signed-off-by" entry is required see [details](http://wiki.eclipse.org/Development_Resources/Contributing_via_Git).
* You can then push your topic branch and its changes into your public fork repository:
````
	$ git push origin my_contribution         # pushes your topic branch into your fork
````
And then [generate a pull-request](http://help.github.com/pull-requests/) where we can
review the proposed changes, comment on them, discuss them with you,
and if everything is good merge the changes right into the official

## Building 

Building _Thym_  requires Maven (3.1+). 

This command will run the build:
````
    $ mvn clean verify
````
