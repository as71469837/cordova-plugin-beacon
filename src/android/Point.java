package huayu.cordova.plugin.beacon;

import retrofit2.http.PUT;

public class Point {
    private double X;

    private double Y;


    public Point(double x,double y){
        this.X=x;
        this.Y=y;
    }

    public double getX()
    {
        return this.X;
    }

    public  double getY(){
        return  this.Y;
    }
}
