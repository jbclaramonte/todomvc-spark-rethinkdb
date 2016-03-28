# todomvc-spark-rethinkdb

docker build -t jbclaramonte/todomvc-spark-rethinkdb .
docker push jbclaramonte/todomvc-spark-rethinkdb

docker run -d -p 8080:8080 -p 28015:28015 -p 29015:29015 rethinkdb
docker run -d -p 4567:4567 -e RETHINKDB_SERVICE_HOST=192.168.99.100 jbclaramonte/todomvc-spark-rethinkdb 



kubectl run rethinkdb --image=rethinkdb --port=28015 --port=8080 
kubectl expose rc/rethinkdb --name=rethinkdb-admin-ui --port=80 --target-port=8080 --type=LoadBalancer
kubectl expose rc/rethinkdb --name=rethinkdb --port=28015 --target-port=28015

kubectl run todomvc-spark-rethinkdb --image=jbclaramonte/todomvc-spark-rethinkdb
## check env var to see RETHINKDB_SERVICE_HOST is available in the container
kubectl exec -it <pod instance> sh
## once inside the container run 'env' command and check the env var

kubectl expose rc/todomvc-spark-rethinkdb --port=80 --target-port=4567 --type=LoadBalancer