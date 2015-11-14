npm install mocha
npm install blanket
npm install chai

mkdir -p target/test-reports/xunit
./node_modules/mocha/bin/mocha --reporter xunit > target/test-reports/xunit/results.xml

mkdir -p target/test-reports/doc
./node_modules/mocha/bin/mocha --reporter doc > target/test-reports/doc/index.html

mkdir -p target/test-reports/coverage_doc
./node_modules/mocha/bin/mocha -r blanket -R html-cov > target/test-reports/coverage_doc/index.html

#./node_modules/mocha/bin/mocha --reporter spec > target/test_spec.txt

true
