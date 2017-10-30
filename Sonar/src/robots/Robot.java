package robots;

import java.util.ArrayList;

public interface Robot {
	/* Faz o movimento até receber o stop */
	public void moveForward ();
	public void moveLeft ();
	public void moveRight ();
	public void moveBackward ();
	public void stop ();
	/* vai x cm para frente e para */
	public void move(double x);
	/* roda x graus no sentido horário e para */
	public void rotate(double x);
	
	/* leitura sensores */
	/**	
	 * @param ini angulo inicial
	 * @param end angulo final
	 * @param interval de quantos em quantos graus vamos realizar a leitura
	 * @return
	 */
	public ArrayList<DataPose> scann (int ini, int end, int interval);
	/* inicia uma leitura conínua do sonar */
	void scann (RobotReturn r);
	/* para uma leitura contínua */
	void stopScann ();
	
	/* funcoes de conexao */
	public boolean connect ();
	public void disconnect ();
	
	/* afimar nova pose para o robo */
	public void setPose(float x, float y, float a);
	
	/* deve retorna o nome do robo */
	public String toString();
	
}
