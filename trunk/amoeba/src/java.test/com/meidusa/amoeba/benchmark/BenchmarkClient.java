package com.meidusa.amoeba.benchmark;

import com.meidusa.amoeba.net.Connection;

public class BenchmarkClient {
	private AbstractBenchmark benchmark;
	private Connection connection;
	public BenchmarkClient(Connection connection,AbstractBenchmark benchmark){
		this.benchmark = benchmark;
		this.connection = connection;
	}
	public AbstractBenchmark getBenchmark() {
		return benchmark;
	}

	public void setBenchmark(AbstractBenchmark benchmark) {
		this.benchmark = benchmark;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	
	
}
